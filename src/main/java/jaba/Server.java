package jaba;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;

public class Server {
	public static Core core;
    static {
        try {
            core = new Core();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }
    }
    
	public static void main(String[] args) throws InterruptedException, ApiException {
		System.out.println("Running server...");
        while (true) {
            Thread.sleep(300);
            try {
                Message message = core.getMessage();
                if (message != null) {
                    ExecutorService exec = Executors.newCachedThreadPool();
                    exec.execute(new Messenger(message));
                }

            } catch (ClientException e) {
                System.out.println("Возникли проблемы");
                final int RECONNECT_TIME = 10000;
                System.out.println("Повторное соединение через " + RECONNECT_TIME / 1000 + " секунд");
                Thread.sleep(RECONNECT_TIME);

            }
        }
	}

}
