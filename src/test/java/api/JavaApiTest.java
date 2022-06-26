package api;

import com.github.zly2006.khlkt.JavaBaseClass;
import com.github.zly2006.khlkt.client.Client;
import com.github.zly2006.khlkt.contract.Self;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import static com.github.zly2006.khlkt.JavaBaseClassKt.connectWebsocket;

public class JavaApiTest extends JavaBaseClass {
    public JavaApiTest() {
        super();
    }

    public static void main(String[] args) throws FileNotFoundException {
        String token = new BufferedReader(new InputStreamReader(new FileInputStream("data/token.txt"))).lines().toList().get(0);
        Self self = connectWebsocket(new Client(token));
    }
}
