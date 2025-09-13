package com.cannestro.drafttable.supporting.csv.strategies;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.lang.reflect.Field;
import java.util.*;

import static java.util.Objects.isNull;


public class AnnotationStrategy extends HeaderColumnNameTranslateMappingStrategy {

    public AnnotationStrategy(Class<?> clazz) {
        Map<String,String> map = new HashMap<>();
        List<String> originalFieldOrder = new ArrayList<>(); //To prevent the column sorting
        for (Field field: clazz.getDeclaredFields()) {
            CsvBindByName annotation = field.getAnnotation(CsvBindByName.class);
            if(!isNull(annotation)) {
                map.put(annotation.column(), annotation.column());
                originalFieldOrder.add(annotation.column());
            }
        }
        setType(clazz);
        setColumnMapping(map);
        setColumnOrderOnWrite(Comparator.comparingInt(originalFieldOrder::indexOf)); //Order the columns as they were created
    }

    @Override
    public String[] generateHeader(Object bean) throws CsvRequiredFieldEmptyException {
        String[] result = super.generateHeader(bean);
        for(int i=0; i < result.length; i++) {
            result[i] = getColumnName(i);
        }
        return result;
    }

}
