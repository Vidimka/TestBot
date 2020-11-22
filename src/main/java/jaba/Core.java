package jaba;

import java.util.List;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.queries.messages.MessagesGetLongPollHistoryQuery;

public class Core {
	private VkApiClient client;
    private static int ts;
    private GroupActor actor;
    private static int maxMsgId = -1;

    public Core() throws ClientException, ApiException {
        HttpTransportClient httpClient = HttpTransportClient.getInstance();
        client = new VkApiClient(httpClient);

        // Загрузка конфигураций

        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("src/main/resources/vkConfig.properties"));
            int groupId = Integer.valueOf(prop.getProperty("groupId"));
            String accessToken = prop.getProperty("accessToken");
            actor = new GroupActor(groupId, accessToken);
            ts = client.messages()
            		.getLongPollServer(actor)
            		.execute()
            		.getTs();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка при загрузке файла конфигурации");

        }
    }
    public Message getMessage() throws ClientException, ApiException {

        MessagesGetLongPollHistoryQuery eventsQuery = client.messages()
                .getLongPollHistory(actor)
                .ts(ts);
        if (maxMsgId > 0){
            eventsQuery.maxMsgId(maxMsgId);
        }
        List<Message> messages = eventsQuery
                .execute()
                .getMessages()
                .getMessages();

        if (!messages.isEmpty()){
            try {
                ts =  client.messages()
                        .getLongPollServer(actor)
                        .execute()
                        .getTs();

            } catch (ClientException e) {
                e.printStackTrace();

            }

        }

        if (!messages.isEmpty() && !messages.get(0).isOut()) {               
            int messageId = messages.get(0).getId();
            if (messageId > maxMsgId){
                maxMsgId = messageId;
            }
            return messages.get(0);
        }
        return null;
    }
}
