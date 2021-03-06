/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.james.jmap.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.apache.james.jmap.model.mailbox.Mailbox;
import org.apache.james.jmap.utils.MailboxUtilsTest;
import org.apache.james.mailbox.MailboxManager;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.inmemory.InMemoryId;
import org.apache.james.mailbox.inmemory.manager.InMemoryIntegrationResources;
import org.apache.james.mailbox.model.MailboxId;
import org.apache.james.mailbox.model.MailboxPath;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailboxFactoryTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailboxUtilsTest.class);

    private MailboxManager mailboxManager;
    private MailboxSession mailboxSession;
    private String user;
    private MailboxFactory sut;

    @Before
    public void setup() throws Exception {
        InMemoryIntegrationResources inMemoryIntegrationResources = new InMemoryIntegrationResources();
        mailboxManager = inMemoryIntegrationResources.createMailboxManager(inMemoryIntegrationResources.createGroupMembershipResolver());
        user = "user@domain.org";
        mailboxSession = mailboxManager.login(user, "pass", LOGGER);
        sut = new MailboxFactory(mailboxManager);
    }


    @Test
    public void mailboxFromMailboxIdShouldReturnAbsentWhenDoesntExist() throws Exception {
        Optional<Mailbox> mailbox = sut.fromMailboxId(InMemoryId.of(123), mailboxSession);
        assertThat(mailbox).isEmpty();
    }

    @Test
    public void mailboxFromMailboxIdShouldReturnPresentWhenExists() throws Exception {
        MailboxPath mailboxPath = new MailboxPath("#private", user, "myBox");
        mailboxManager.createMailbox(mailboxPath, mailboxSession);
        MailboxId mailboxId = mailboxManager.getMailbox(mailboxPath, mailboxSession).getId();

        Optional<Mailbox> mailbox = sut.fromMailboxId(mailboxId, mailboxSession);
        assertThat(mailbox).isPresent();
        assertThat(mailbox.get().getId()).isEqualTo(mailboxId);
    }

    @Test
    public void mailboxFromMailboxPathShouldReturnNotEmptyWhenMailboxExists() throws Exception {
        MailboxPath mailboxPath = new MailboxPath("#private", user, "mailbox");
        mailboxManager.createMailbox(mailboxPath, mailboxSession);

        Optional<Mailbox> optionalMailbox = sut.fromMailboxPath(mailboxPath, mailboxSession);
        assertThat(optionalMailbox).isPresent();
    }

    @Test
    public void mailboxFromMailboxPathShouldReturnEmptyWhenMailboxDoesntExist() throws Exception {
        MailboxPath mailboxPath = new MailboxPath("#private", user, "mailbox");

        Optional<Mailbox> optionalMailbox = sut.fromMailboxPath(mailboxPath, mailboxSession);
        assertThat(optionalMailbox).isEmpty();
    }


    @Test
    public void getNameShouldReturnMailboxNameWhenRootMailbox() throws Exception {
        String expected = "mailbox";
        MailboxPath mailboxPath = new MailboxPath("#private", user, expected);

        String name = sut.getName(mailboxPath, mailboxSession);
        assertThat(name).isEqualTo(expected);
    }

    @Test
    public void getNameShouldReturnMailboxNameWhenChildMailbox() throws Exception {
        String expected = "mailbox";
        MailboxPath mailboxPath = new MailboxPath("#private", user, "inbox." + expected);

        String name = sut.getName(mailboxPath, mailboxSession);
        assertThat(name).isEqualTo(expected);
    }

    @Test
    public void getNameShouldReturnMailboxNameWhenChildOfChildMailbox() throws Exception {
        String expected = "mailbox";
        MailboxPath mailboxPath = new MailboxPath("#private", user, "inbox.children." + expected);

        String name = sut.getName(mailboxPath, mailboxSession);
        assertThat(name).isEqualTo(expected);
    }

    @Test
    public void getParentIdFromMailboxPathShouldReturNullWhenRootMailbox() throws Exception {
        MailboxPath mailboxPath = new MailboxPath("#private", user, "mailbox");
        mailboxManager.createMailbox(mailboxPath, mailboxSession);

        Optional<MailboxId> id = sut.getParentIdFromMailboxPath(mailboxPath, mailboxSession);
        assertThat(id).isEmpty();
    }

    @Test
    public void getParentIdFromMailboxPathShouldReturnParentIdWhenChildMailbox() throws Exception {
        MailboxPath parentMailboxPath = new MailboxPath("#private", user, "inbox");
        mailboxManager.createMailbox(parentMailboxPath, mailboxSession);
        MailboxId parentId = mailboxManager.getMailbox(parentMailboxPath, mailboxSession).getId();

        MailboxPath mailboxPath = new MailboxPath("#private", user, "inbox.mailbox");
        mailboxManager.createMailbox(mailboxPath, mailboxSession);

        Optional<MailboxId> id = sut.getParentIdFromMailboxPath(mailboxPath, mailboxSession);
        assertThat(id).contains(parentId);
    }

    @Test
    public void getParentIdFromMailboxPathShouldReturnParentIdWhenChildOfChildMailbox() throws Exception {
        MailboxPath mailboxPath = new MailboxPath("#private", user, "inbox.children.mailbox");
        mailboxManager.createMailbox(new MailboxPath("#private", user, "inbox"), mailboxSession);

        MailboxPath parentMailboxPath = new MailboxPath("#private", user, "inbox.children");
        mailboxManager.createMailbox(parentMailboxPath, mailboxSession);
        MailboxId parentId = mailboxManager.getMailbox(parentMailboxPath, mailboxSession).getId();

        mailboxManager.createMailbox(mailboxPath, mailboxSession);

        Optional<MailboxId> id = sut.getParentIdFromMailboxPath(mailboxPath, mailboxSession);
        assertThat(id).contains(parentId);
    }

}
