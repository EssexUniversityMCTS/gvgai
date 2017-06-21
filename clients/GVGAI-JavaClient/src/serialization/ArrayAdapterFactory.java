package serialization;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import utils.com.google.gson.Gson;
import utils.com.google.gson.TypeAdapter;
import utils.com.google.gson.TypeAdapterFactory;
import utils.com.google.gson.reflect.TypeToken;

/***
 * This class is used to generate GSon objects using the custom ArrayAdapter class rules.
 */
public class ArrayAdapterFactory implements TypeAdapterFactory {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {

        TypeAdapter<T> typeAdapter = null;

        try {
            if (type.getRawType() == List.class)
                typeAdapter = new ArrayAdapter(
                        (Class) ((ParameterizedType) type.getType())
                                .getActualTypeArguments()[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return typeAdapter;
    }

}
