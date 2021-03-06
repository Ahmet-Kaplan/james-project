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

package org.apache.james.mailbox.elasticsearch.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.time.ZoneId;

import javax.mail.Flags;

import org.apache.commons.io.IOUtils;
import org.apache.james.mailbox.MessageUid;
import org.apache.james.mailbox.elasticsearch.IndexAttachments;
import org.apache.james.mailbox.mock.MockMailboxSession;
import org.apache.james.mailbox.model.TestId;
import org.apache.james.mailbox.store.extractor.DefaultTextExtractor;
import org.apache.james.mailbox.store.mail.model.MailboxMessage;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class IndexableMessageTest {

    public static final MessageUid MESSAGE_UID = MessageUid.of(154);

    @Test
    public void textShouldBeEmptyWhenNoMatchingHeaders() throws Exception {
        MailboxMessage mailboxMessage = mock(MailboxMessage.class);
        TestId mailboxId = TestId.of(1);
        when(mailboxMessage.getMailboxId())
            .thenReturn(mailboxId);
        when(mailboxMessage.getFullContent())
            .thenReturn(new ByteArrayInputStream("".getBytes()));
        when(mailboxMessage.createFlags())
            .thenReturn(new Flags());
        when(mailboxMessage.getUid())
            .thenReturn(MESSAGE_UID);

        IndexableMessage indexableMessage = IndexableMessage.from(mailboxMessage, ImmutableList.of(new MockMailboxSession("username").getUser()),
                new DefaultTextExtractor(), ZoneId.of("Europe/Paris"), IndexAttachments.NO);

        assertThat(indexableMessage.getText()).isEmpty();
    }

    @Test
    public void textShouldContainsFromWhenFrom() throws Exception {
        MailboxMessage mailboxMessage = mock(MailboxMessage.class);
        TestId mailboxId = TestId.of(1);
        when(mailboxMessage.getMailboxId())
            .thenReturn(mailboxId);
        when(mailboxMessage.getFullContent())
            .thenReturn(new ByteArrayInputStream("From: First user <user@james.org>\nFrom: Second user <user2@james.org>".getBytes()));
        when(mailboxMessage.createFlags())
            .thenReturn(new Flags());
        when(mailboxMessage.getUid())
            .thenReturn(MESSAGE_UID);

        IndexableMessage indexableMessage = IndexableMessage.from(mailboxMessage, ImmutableList.of(new MockMailboxSession("username").getUser()),
                new DefaultTextExtractor(), ZoneId.of("Europe/Paris"), IndexAttachments.NO);

        assertThat(indexableMessage.getText()).isEqualTo("Second user user2@james.org First user user@james.org");
    }

    @Test
    public void textShouldContainsToWhenTo() throws Exception {
        MailboxMessage mailboxMessage = mock(MailboxMessage.class);
        TestId mailboxId = TestId.of(1);
        when(mailboxMessage.getMailboxId())
            .thenReturn(mailboxId);
        when(mailboxMessage.getFullContent())
            .thenReturn(new ByteArrayInputStream("To: First to <user@james.org>\nTo: Second to <user2@james.org>".getBytes()));
        when(mailboxMessage.createFlags())
            .thenReturn(new Flags());
        when(mailboxMessage.getUid())
            .thenReturn(MESSAGE_UID);

        IndexableMessage indexableMessage = IndexableMessage.from(mailboxMessage, ImmutableList.of(new MockMailboxSession("username").getUser()),
                new DefaultTextExtractor(), ZoneId.of("Europe/Paris"), IndexAttachments.NO);

        assertThat(indexableMessage.getText()).isEqualTo("First to user@james.org Second to user2@james.org");
    }

    @Test
    public void textShouldContainsCcWhenCc() throws Exception {
        MailboxMessage mailboxMessage = mock(MailboxMessage.class);
        TestId mailboxId = TestId.of(1);
        when(mailboxMessage.getMailboxId())
            .thenReturn(mailboxId);
        when(mailboxMessage.getFullContent())
            .thenReturn(new ByteArrayInputStream("Cc: First cc <user@james.org>\nCc: Second cc <user2@james.org>".getBytes()));
        when(mailboxMessage.createFlags())
            .thenReturn(new Flags());
        when(mailboxMessage.getUid())
            .thenReturn(MESSAGE_UID);

        IndexableMessage indexableMessage = IndexableMessage.from(mailboxMessage, ImmutableList.of(new MockMailboxSession("username").getUser()),
                new DefaultTextExtractor(), ZoneId.of("Europe/Paris"), IndexAttachments.NO);

        assertThat(indexableMessage.getText()).isEqualTo("First cc user@james.org Second cc user2@james.org");
    }

    @Test
    public void textShouldContainsBccWhenBcc() throws Exception {
        MailboxMessage mailboxMessage = mock(MailboxMessage.class);
        TestId mailboxId = TestId.of(1);
        when(mailboxMessage.getMailboxId())
            .thenReturn(mailboxId);
        when(mailboxMessage.getUid())
            .thenReturn(MESSAGE_UID);
        when(mailboxMessage.getFullContent())
            .thenReturn(new ByteArrayInputStream("Bcc: First bcc <user@james.org>\nBcc: Second bcc <user2@james.org>".getBytes()));
        when(mailboxMessage.createFlags())
            .thenReturn(new Flags());

        IndexableMessage indexableMessage = IndexableMessage.from(mailboxMessage, ImmutableList.of(new MockMailboxSession("username").getUser()),
                new DefaultTextExtractor(), ZoneId.of("Europe/Paris"), IndexAttachments.NO);

        assertThat(indexableMessage.getText()).isEqualTo("Second bcc user2@james.org First bcc user@james.org");
    }

    @Test
    public void textShouldContainsSubjectsWhenSubjects() throws Exception {
        MailboxMessage mailboxMessage = mock(MailboxMessage.class);
        TestId mailboxId = TestId.of(1);
        when(mailboxMessage.getMailboxId())
            .thenReturn(mailboxId);
        when(mailboxMessage.getFullContent())
            .thenReturn(new ByteArrayInputStream("Subject: subject1\nSubject: subject2".getBytes()));
        when(mailboxMessage.createFlags())
            .thenReturn(new Flags());
        when(mailboxMessage.getUid())
            .thenReturn(MESSAGE_UID);

        IndexableMessage indexableMessage = IndexableMessage.from(mailboxMessage, ImmutableList.of(new MockMailboxSession("username").getUser()),
                new DefaultTextExtractor(), ZoneId.of("Europe/Paris"), IndexAttachments.NO);

        assertThat(indexableMessage.getText()).isEqualTo("subject1 subject2");
    }

    @Test
    public void textShouldContainsBodyWhenBody() throws Exception {
        MailboxMessage mailboxMessage = mock(MailboxMessage.class);
        TestId mailboxId = TestId.of(1);
        when(mailboxMessage.getMailboxId())
            .thenReturn(mailboxId);
        when(mailboxMessage.getFullContent())
            .thenReturn(new ByteArrayInputStream("\nMy body".getBytes()));
        when(mailboxMessage.createFlags())
            .thenReturn(new Flags());
        when(mailboxMessage.getUid())
            .thenReturn(MESSAGE_UID);

        IndexableMessage indexableMessage = IndexableMessage.from(mailboxMessage, ImmutableList.of(new MockMailboxSession("username").getUser()),
                new DefaultTextExtractor(), ZoneId.of("Europe/Paris"), IndexAttachments.NO);

        assertThat(indexableMessage.getText()).isEqualTo("My body");
    }

    @Test
    public void textShouldContainsAllFieldsWhenAllSet() throws Exception {
        MailboxMessage mailboxMessage = mock(MailboxMessage.class);
        TestId mailboxId = TestId.of(1);
        when(mailboxMessage.getMailboxId())
            .thenReturn(mailboxId);
        when(mailboxMessage.getFullContent())
            .thenReturn(new ByteArrayInputStream(IOUtils.toByteArray(ClassLoader.getSystemResourceAsStream("eml/mailWithHeaders.eml"))));
        when(mailboxMessage.createFlags())
            .thenReturn(new Flags());
        when(mailboxMessage.getUid())
            .thenReturn(MESSAGE_UID);

        IndexableMessage indexableMessage = IndexableMessage.from(mailboxMessage, ImmutableList.of(new MockMailboxSession("username").getUser()),
                new DefaultTextExtractor(), ZoneId.of("Europe/Paris"), IndexAttachments.NO);

        assertThat(indexableMessage.getText()).isEqualTo("Ad Min admin@opush.test " +
                "a@test a@test B b@test " + 
                "c@test c@test " +
                "dD d@test " + 
                "my subject " + 
                "Mail content\n" +
                "\n" +
                "-- \n" + 
                "Ad Min\n");
    }

    @Test
    public void attachmentsShouldNotBeenIndexedWhenAsked() throws Exception {
        //Given
        MailboxMessage mailboxMessage = mock(MailboxMessage.class);
        TestId mailboxId = TestId.of(1);
        when(mailboxMessage.getMailboxId())
            .thenReturn(mailboxId);
        when(mailboxMessage.getFullContent())
            .thenReturn(new ByteArrayInputStream(IOUtils.toByteArray(ClassLoader.getSystemResourceAsStream("eml/Toto.eml"))));
        when(mailboxMessage.createFlags())
            .thenReturn(new Flags());
        when(mailboxMessage.getUid())
            .thenReturn(MESSAGE_UID);

        // When
        IndexableMessage indexableMessage = IndexableMessage.from(mailboxMessage, ImmutableList.of(new MockMailboxSession("username").getUser()),
                new DefaultTextExtractor(), ZoneId.of("Europe/Paris"), IndexAttachments.NO);

        // Then
        assertThat(indexableMessage.getAttachments()).isEmpty();
    }

    @Test
    public void attachmentsShouldBeenIndexedWhenAsked() throws Exception {
        //Given
        MailboxMessage mailboxMessage = mock(MailboxMessage.class);
        TestId mailboxId = TestId.of(1);
        when(mailboxMessage.getMailboxId())
            .thenReturn(mailboxId);
        when(mailboxMessage.getFullContent())
            .thenReturn(new ByteArrayInputStream(IOUtils.toByteArray(ClassLoader.getSystemResourceAsStream("eml/Toto.eml"))));
        when(mailboxMessage.createFlags())
            .thenReturn(new Flags());
        when(mailboxMessage.getUid())
            .thenReturn(MESSAGE_UID);

        // When
        IndexableMessage indexableMessage = IndexableMessage.from(mailboxMessage, ImmutableList.of(new MockMailboxSession("username").getUser()),
                new DefaultTextExtractor(), ZoneId.of("Europe/Paris"), IndexAttachments.YES);

        // Then
        assertThat(indexableMessage.getAttachments()).isNotEmpty();
    }
}
