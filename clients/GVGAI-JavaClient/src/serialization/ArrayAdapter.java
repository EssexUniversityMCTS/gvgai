package serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.com.google.gson.Gson;
import utils.com.google.gson.GsonBuilder;
import utils.com.google.gson.TypeAdapter;
import utils.com.google.gson.stream.JsonReader;
import utils.com.google.gson.stream.JsonToken;
import utils.com.google.gson.stream.JsonWriter;

/***
 * This is a custom array adapter used to interpret GSon arrays in a special, tokenized manner.
 * It is used to avoid deserialization errors (since the standard adapter cannot handle
 * array lists well enough).
 * @param <T>
 */
public class ArrayAdapter<T> extends TypeAdapter<List<T>> {
    private Class<T> adapterclass;

    public ArrayAdapter(Class<T> adapterclass) {
        this.adapterclass = adapterclass;
    }

    public List<T> read(JsonReader reader) throws IOException {

        List<T> list = new ArrayList<T>();

        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new ArrayAdapterFactory())
                .create();

        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            T inning = gson.fromJson(reader, adapterclass);
            list.add(inning);

        } else if (reader.peek() == JsonToken.BEGIN_ARRAY) {

            reader.beginArray();
            while (reader.hasNext()) {
                T inning = gson.fromJson(reader, adapterclass);
                list.add(inning);
            }
            reader.endArray();

        }

        return list;
    }

    public void write(JsonWriter writer, List<T> value) throws IOException {

    }

}

