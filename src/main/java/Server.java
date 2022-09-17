import com.google.gson.GsonBuilder;

public class Server {

    public static void main(String[] args) {

        var gsonb = new GsonBuilder();
        gsonb.registerTypeAdapterFactory(new RecordTypeAdapterFactory());
        var gson = gsonb.create();

        {
            var reqString = "{\"operation\": \"CREATE_USER\", \"username\": \"admin\", \"password\": \"123\"}";

            var obj = new CreateUserHandler().handle(reqString);
            var res = gson.toJson(obj);
            System.out.println(res);
        }

        String token = "";

        {
            var reqString = "{\"operation\": \"CREATE_SESSION\", \"username\": \"admin\", \"password\": \"123\"}";
            var obj = new CreateSessionHandler().handle(reqString);
            var res = gson.toJson(obj);
            System.out.println(res);
            token = ((CreateSessionHandler.ResponseData) obj).token();
        }

        {
            var reqString = String.format("{\"operation\": \"SAY_HELLO\", \"token\": \"%s\"}", token);
            var res = gson.toJson(new SayHelloHandler().handle(reqString));
            System.out.println(res);
        }

        {
            var reqString = "{\"operation\": \"SAY_HELLO\", \"token\": \"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"}";
            var res = gson.toJson(new SayHelloHandler().handle(reqString));
            System.out.println(res);
        }

    }
}
