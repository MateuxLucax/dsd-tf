import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

public class Server {

    public static void main(String[] args) {

        var gsonb = new GsonBuilder();
        gsonb.registerTypeAdapterFactory(new RecordTypeAdapterFactory());
        var gson = gsonb.create();

        var reqString = "{\"operation\": \"CREATE_USER\", \"username\": \"admin\", \"password\": \"123\"}";
        var req = JsonParser.parseString(reqString);

        var obj = new CreateUserHandler().handle(req);
        var res = gson.toJson(obj);

        System.out.println(res);
    }
}
