package asia.sweethome.chat.service;


import asia.sweethome.api.FamilyApi;
import asia.sweethome.api.UserApi;
import asia.sweethome.api.entity.dto.RelationDTO;
import asia.sweethome.api.entity.dto.UserDTO;
import asia.sweethome.chat.entity.po.Message;
import asia.sweethome.chat.entity.vo.MessageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatAssemblerTest {

    @Mock IConversationMembersService conversationMembersService;
    @Mock IMessagesService messagesService;
    @Mock UserApi userApi;
    @Mock FamilyApi familyAPi;

    ChatAssembler chatAssembler;

    @BeforeEach
    void setup(){
        chatAssembler = new ChatAssembler(
                conversationMembersService,
                messagesService
        );

        ReflectionTestUtils.setField(
                chatAssembler,
                "userApi",
                userApi
        );

        ReflectionTestUtils.setField(
                chatAssembler,
                "familyApi",
                familyAPi
        );

    }

    // 组装消息
    @Test
    void testChatAssemble(){
        Message msg = new Message();

        msg.setId(100L);
        msg.setSenderId(19L);
        msg.setContent("晚饭吃了吗");

        UserDTO sender = new UserDTO();
        sender.setName("张三");

        when(userApi.findUserById(19L)).thenReturn(
                sender
        );

        when(familyAPi.getRelation(any())).thenReturn(
                new RelationDTO("F")
        );

        MessageVO vo = chatAssembler.toMessageVO(
                msg, 1L, "zh"
        );

        assertThat( vo.getSenderName() ).isEqualTo("张三");
        assertThat( vo.getSenderRelationCode() ).isEqualTo("F");
        assertThat( vo.getContent() ).isEqualTo("晚饭吃了吗");

    }

}