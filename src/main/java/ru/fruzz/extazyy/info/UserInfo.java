package ru.fruzz.extazyy.info;

import lombok.Getter;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

public class UserInfo {
   @Getter
   private String name;
   @Getter
   private int uid;
   @Getter
   private String role;
   @Getter
   private int till;

   @Getter
   private boolean hasAdmin;

    public String getTill() {
      Instant instant = Instant.ofEpochSecond(till);
      Date tillstring = Date.from(instant);
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
      return dateFormat.format(tillstring);
   }

   public UserInfo(String name, int uid, String role, int till, boolean hasAdmin) {
      this.name = name;
      this.uid = uid;
      this.role = role;
      this.till = till;
      this.hasAdmin = hasAdmin;
   }


}
