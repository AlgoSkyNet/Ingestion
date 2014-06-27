/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.ingestion.source.generator;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.flume.ChannelException;

import java.util.Date;
import java.util.List;
import java.util.Random;

public class RandomFieldsGenerator {
   static Random rand = new Random();

   public static String generateRandomField(GeneratorField generatorField) throws ChannelException {
       StringBuilder randomString = new StringBuilder();
       String fieldType = generatorField.getType();
       switch (fieldType) {
           case "string" : randomString.append(generateRandomString(generatorField));
               break;
           case "integer" : randomString.append(generateRandomInteger(generatorField));
               break;
           case "list" : randomString.append(generateRandomElementFromList(generatorField));
               break;
           case "ip" : randomString.append(generateRandomIp());
               break;
           case "date" : randomString.append(generateRandomDate(generatorField));
               break;

       }
      return randomString.toString();
   }

   private static String generateRandomString(GeneratorField generatorField) throws ChannelException {
       try {
        int length = Integer.parseInt(getPropertyValue(generatorField.getProperties(), 0));
        return RandomStringUtils.randomAlphanumeric(length);
       } catch(NumberFormatException nFEx) {
           throw new ChannelException(nFEx.getMessage());
       }

   }

    private static String generateRandomInteger(GeneratorField generatorField) throws ChannelException {
        try {
            int length = Integer.parseInt(getPropertyValue(generatorField.getProperties(), 0));
            return RandomStringUtils.randomNumeric(length);
        } catch(NumberFormatException nFEx) {
            throw new ChannelException(nFEx.getMessage());
        }
    }

    private static String getPropertyValue(List<FieldProperty> properties, int propertyIndex) {
        FieldProperty fieldProperty = properties.get(propertyIndex);
        return fieldProperty.getPropertyValue();
    }

    private static String getPropertyName(List<FieldProperty> properties, int propertyIndex) {
        FieldProperty fieldProperty = properties.get(propertyIndex);
        return fieldProperty.getPropertyName();
    }

    private static String generateRandomElementFromList(GeneratorField generatorField) throws ChannelException {
        if (getPropertyName(generatorField.getProperties(), 0).equals("values"))
            return getRandomElementFromListOfValues(generatorField);
        else
            return getRandomElementFromFile(generatorField);
    }

    private static String getRandomElementFromListOfValues(GeneratorField generatorField) {
    try {
            String stringValues = getPropertyValue(generatorField.getProperties(), 0);
            String[] availableStrings = stringValues.split(",");
            int selectedRandomValue= rand.nextInt(availableStrings.length);
            return availableStrings[selectedRandomValue].trim();
        } catch(NumberFormatException nFEx) {
            throw new ChannelException(nFEx.getMessage());
        }
    }

    private static String getRandomElementFromFile(GeneratorField generatorField) {
        try {
            String filePath = getPropertyValue(generatorField.getProperties(), 0);
            return FileLoader.getRandomElementFromFile(filePath);
        } catch(Exception ex) {
            throw new ChannelException(ex.getMessage());
        }
    }

    public static String generateRandomIp() throws ChannelException {
        try {
            Random r = new Random();
            return r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
        } catch(NumberFormatException nFEx) {
            throw new ChannelException(nFEx.getMessage());
        }
    }

    public static String generateRandomDate(GeneratorField generatorField) throws ChannelException {
        String dateFormat = getPropertyValue(generatorField.getProperties(), 0);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(dateFormat);
        return sdf.format(new Date());
    }
}
