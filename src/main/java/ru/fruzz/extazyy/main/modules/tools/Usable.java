package ru.fruzz.extazyy.main.modules.tools;



import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;

@Getter
public class Usable {
    public ArrayList<Tools> toolsList = new ArrayList<>();
    public final void addSettings(Tools... options) {
        toolsList.addAll(Arrays.asList(options));
    }


}
