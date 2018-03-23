/*
 * Copyright 2006-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.ftp.server;

import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.ftp.client.FtpEndpointConfiguration;
import com.consol.citrus.ftp.message.FtpMessage;
import com.consol.citrus.ftp.model.*;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.ftpserver.filesystem.nativefs.NativeFileSystemFactory;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.impl.LocalizedFileActionFtpReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.xml.transform.StringResult;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Ftp servlet implementation that logs incoming connections and commands forwarding those to
 * endpoint adapter for processing in test case.
 *
 * Test case can manage the Ftp command result by providing a Ftp result message.
 *
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class FtpServerFtpLet implements Ftplet {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(FtpServerFtpLet.class);

    /** Endpoint configuration */
    private final FtpEndpointConfiguration endpointConfiguration;

    /** Endpoint adapter */
    private final EndpointAdapter endpointAdapter;

    /**
     * Constructor using the server's endpoint adapter implementation.
     * @param endpointConfiguration
     * @param endpointAdapter
     */
    public FtpServerFtpLet(FtpEndpointConfiguration endpointConfiguration, EndpointAdapter endpointAdapter) {
        this.endpointConfiguration = endpointConfiguration;
        this.endpointAdapter = endpointAdapter;
    }

    public FtpMessage handleMessage(FtpMessage request) {
        if (request.getPayload() instanceof Command) {
            StringResult result = new StringResult();
            endpointConfiguration.getMarshaller().marshal(request.getPayload(Command.class), result);
            request.setPayload(result.toString());
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Received request on ftp server: '%s':%n%s",
                    request.getSignal(),
                    request.getPayload(String.class)));
        }

        return Optional.ofNullable(endpointAdapter.handleMessage(request))
                .map(response -> {
                    if (response instanceof FtpMessage) {
                        return (FtpMessage) response;
                    } else {
                        return new FtpMessage(response);
                    }
                })
                .orElse(FtpMessage.result());
    }

    @Override
    public void init(FtpletContext ftpletContext) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Total FTP logins: %s", ftpletContext.getFtpStatistics().getTotalLoginNumber()));
        }
    }

    @Override
    public void destroy() {
        log.info("FTP server shutting down ...");
    }

    @Override
    public FtpletResult beforeCommand(FtpSession session, FtpRequest request) {
        String command = request.getCommand().toUpperCase();

        if (log.isDebugEnabled()) {
            log.debug(String.format("Received FTP command: '%s'", command));
        }

        if (endpointConfiguration.isAutoLogin() && (command.equals(FTPCmd.USER.getCommand()) || command.equals(FTPCmd.PASS.getCommand()))) {
            return FtpletResult.DEFAULT;
        }

        FtpReply reply = getFtpReply(handleMessage(FtpMessage.command(FTPCmd.valueOf(command)).arguments(request.getArgument())), session);

        try {
            session.write(reply);
        } catch (FtpException e) {
            throw new CitrusRuntimeException("Failed to write ftp reply", e);
        }

        return FtpletResult.SKIP;
    }

    @Override
    public FtpletResult afterCommand(FtpSession session, FtpRequest request, FtpReply reply) {
        return FtpletResult.DEFAULT;
    }

    private FtpReply getFtpReply(FtpMessage ftpMessage, FtpSession session) {
        CommandResultType commandResult = ftpMessage.getPayload(CommandResultType.class);

        try {
            if (commandResult instanceof GetCommandResult) {
                return new LocalizedFileActionFtpReply(Integer.valueOf(commandResult.getReplyCode()), commandResult.getReplyString(),
                        new NativeFileSystemFactory().createFileSystemView(session.getUser()).getFile(((GetCommandResult) commandResult).getFile().getPath()));
            } else if (commandResult instanceof ListCommandResult) {
                return new DefaultFtpReply(Integer.valueOf(commandResult.getReplyCode()),
                        ((ListCommandResult) commandResult).getFiles().getFiles().stream()
                                                                                    .map(ListCommandResult.Files.File::getPath)
                                                                                    .collect(Collectors.joining(" ")));
            }
        } catch (FtpException e) {
            throw new CitrusRuntimeException("Failed to get file from local file system view", e);
        }

        return new DefaultFtpReply(Integer.valueOf(commandResult.getReplyCode()), commandResult.getReplyString());
    }

    @Override
    public FtpletResult onConnect(FtpSession session) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Received new FTP connection: '%s'", session.getSessionId()));
        }

        if (!endpointConfiguration.isAutoConnect()) {
            FtpReply reply = getFtpReply(handleMessage(FtpMessage.command(FTPCmd.PASS).arguments(session.getUser().getName() + ":" + session.getUser().getPassword())), session);

            try {
                session.write(reply);
            } catch (FtpException e) {
                throw new CitrusRuntimeException("Failed to write ftp reply", e);
            }

            return FtpletResult.SKIP;
        }

        return FtpletResult.DEFAULT;
    }

    @Override
    public FtpletResult onDisconnect(FtpSession session) {
        if (!endpointConfiguration.isAutoConnect()) {
            FtpReply reply = getFtpReply(handleMessage(FtpMessage.command(FTPCmd.QUIT).arguments(session.getUser().getName() + ":" + session.getUser().getPassword())), session);

            try {
                session.write(reply);
            } catch (FtpException e) {
                throw new CitrusRuntimeException("Failed to write ftp reply", e);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Closing FTP connection: '%s'", session.getSessionId()));
        }

        return FtpletResult.DISCONNECT;
    }
}
