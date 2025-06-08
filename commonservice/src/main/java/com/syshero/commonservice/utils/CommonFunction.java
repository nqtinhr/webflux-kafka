package com.syshero.commonservice.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.syshero.commonservice.common.ValidateException;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@NoArgsConstructor
public class CommonFunction {

    @SneakyThrows
    public static void jsonValidate(InputStream inputStream, String json) {
        // Tạo một JsonSchema từ InputStream với v7 của file schema
        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7).getSchema(inputStream);
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(json); // Đọc chuỗi JSON thành JsonNode
        Set<ValidationMessage> errors = schema.validate(jsonNode); // validate request
        Map<String, String> stringSetMap = new HashMap<>();
        for (ValidationMessage error : errors) {
            String path = formatStringValidate(error.getInstanceLocation().toString());

            if (stringSetMap.containsKey(path)) {
                String message = stringSetMap.get(path);
                stringSetMap.put(path, message + ", " + formatStringValidate(error.getMessage()));
            } else {
                stringSetMap.put(path, formatStringValidate(error.getMessage()));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidateException("RQ01", stringSetMap, HttpStatus.BAD_REQUEST);
        }
    }

    public static String formatStringValidate(String message) {
        return message.replaceAll("\\$.", "");
    }
}
