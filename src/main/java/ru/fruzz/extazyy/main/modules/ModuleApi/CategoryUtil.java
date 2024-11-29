package ru.fruzz.extazyy.main.modules.ModuleApi;

import ru.fruzz.extazyy.misc.util.anim.Animation;

public enum CategoryUtil {
    Combat("General", "B",0f,8.3f),
    Movement("General", "C", 0f, 8.3f),
    Render("General", "D", 0f, 8.3f),
    Player("General", "E", 0f,8.3f),
    Misc("General", "F", 0f,8.3f),
    Cloud("Cloud", "G", 0f,8.3f);



    public final String description;
    public final String image;
    public double animf;
    public double offset;
    public Animation anim = new Animation(Animation.Ease.LINEAR, 0,1, 100);

    CategoryUtil(String description, String image, double animf, float offset) {
        this.animf = animf;
        this.offset = offset;
        this.description = description;
        this.image = image;
    }
}
