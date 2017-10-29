package panasuk.bd7;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    String filename = "Notes.json";
    File file;
    HashMap<String, String> notes;
    CalendarView calendar;
    TextView note;
    EditText text;
    final String[] date = new String[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        file = new File(this.getFilesDir(), filename);
        notes = new HashMap<>();
        calendar = (CalendarView) findViewById(R.id.calendarView);
        note = (TextView) findViewById(R.id.textNote);
        text = (EditText) findViewById(R.id.editNote);
        if(!existFile(file)){
            try {
                createFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else{
            try {
                fillMap();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                date[0] = String.valueOf(year) + String.valueOf(month) + String.valueOf(dayOfMonth);
                String noteText = notes.get(date[0]);
                if(noteText != null){
                    note.setText(noteText);
                }
                else {
                    note.setText("");
                }
            }
        });
    }

    public void onAdd(View view) throws IOException, ParseException {
        if(date[0] == null){
            showToast("не выбрана дата");
            return;
        }

        if (text.getText().toString().trim().length() == 0) {
            showToast("Введите заметку");
            return;
        }

        notes.put(date[0], text.getText().toString());
        writeToJSON(date[0], text.getText().toString());

        note.setText(text.getText());
        text.setText("");
    }

    public void onDelete(View view) throws IOException, ParseException {
        if(date[0] == null){
            showToast("не выбрана дата");
            return;
        }

        if(note.getText().toString().trim().length() == 0){
            showToast("Нет заметки для удаления");
            return;
        }

        notes.remove(date[0]);
        file.delete();
        createFile();

        for(Map.Entry<String, String> item : notes.entrySet()){
            writeToJSON(item.getKey(), item.getValue());
        }

        text.setText("");
        note.setText("");
        showToast("Заметка удалена");
    }

    public void onChange(View view) throws IOException, ParseException {
        if(date[0] == null){
            showToast("Не выбрана дата");
            return;
        }

        if(notes.get(date[0]) == null){
            showToast("");
            return;
        }

        notes.remove(date[0]);
        notes.put(date[0], text.getText().toString());
        file.delete();
        createFile();

        for(Map.Entry<String, String> item : notes.entrySet()){
            writeToJSON(item.getKey(), item.getValue());
        }

        text.setText("");
        note.setText(notes.get(date[0]));
        showToast("Заметка отредактирована");
    }

    private boolean existFile(File file){
        return file.exists();
    }

    private void showToast(String text){
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 8);
        toast.show();
    }

    private void createFile() throws IOException{
        file.createNewFile();

        JSONObject mainObj = new JSONObject();
        JSONObject tempObj = new JSONObject();
        JSONArray array = new JSONArray();

        tempObj.put("", "");
        mainObj.put("Notes", array);
        try(FileWriter writer = new FileWriter(file)){
            writer.write(mainObj.toJSONString());
            writer.close();
        } catch (IOException ex){
            showToast("Не удалось записать данные");
        }
    }

    private void fillMap() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(
                new FileReader(file));
        JSONArray array = (JSONArray) object.get("Notes");
        String key, note;

        if(array.isEmpty()){
            return;
        }

        for(Object obj : array){
            key = ((JSONObject)obj).get("date").toString();
            note = ((JSONObject)obj).get("note").toString();
            notes.put(key, note);
        }
    }

    private void writeToJSON(String date, String note) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject mainObj = (JSONObject) parser.parse(new FileReader(file));
        JSONObject tempObj = new JSONObject();
        JSONArray array = (JSONArray) mainObj.get("Notes");

        tempObj.put("date", date);
        tempObj.put("note", note);

        array.add(tempObj);
        mainObj.put("Notes", array);

        try(FileWriter writer = new FileWriter(file)){
            writer.write(mainObj.toJSONString());
            writer.close();
            showToast("Заметка записана");
        } catch (IOException e){
            showToast("Не удалось записать данные");
        }
    }
}
