package org.bralax.endpoint;

public class Parameter {
   private String name;
   private String description;
   private String type;
   private boolean required;
   
   public Parameter(String name, String description) {
      this(name, description, "String", false);
   }

   public Parameter(String name, String description, String type, boolean required) {
      this.name = name;
      this.description = description;
      this.required = required;
   }
   
   public String getName() {
      return name;
   }
   
   public String getDescription() {
      return description;
   }

   public String getType() {
      return type;
   }
   
   public boolean getRequired() {
      return required;
   }

   public void setName(String name) {
       this.name = name;
   }
   
   public void setDescription(String description) {
       this.description = description;
   }

   public void setType(String type) {
      this.type = type;
  }

   public void setRequired(boolean req) {
      this.required = req;
   }
   
   @Override
   public String toString() {
      return "{Name: "+this.name+", Description: "+this.description+"}";
   }

}