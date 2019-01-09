package com.ctrip.ferriswheel.proto.util;

import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.proto.v1.UnionValue;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.*;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicCodec {
    private static final Logger LOG = LoggerFactory.getLogger(DynamicCodec.class);
    private static final String DEFAULT_DESC_FILE = "spreadsheet.desc";

    private static final String BASE_PACKAGE_NAME = "com.ctrip.ferriswheel.proto";
    private static final String MESSAGE_TYPE_INSERT_ROWS = BASE_PACKAGE_NAME + ".InsertRows";

    private final Map<String, FileDescriptor> fileDescriptorMap = new ConcurrentHashMap<>();
    private final Map<String, Descriptor> typeMap = new ConcurrentHashMap<>();
    private final Map<String, EnumDescriptor> enumMap = new ConcurrentHashMap<>();

    DynamicCodec() throws IOException, DescriptorValidationException {
        InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(DEFAULT_DESC_FILE);
        if (in == null) {
            in = DynamicCodec.class.getResourceAsStream(DEFAULT_DESC_FILE);
        }
        if (in == null) {
            throw new IOException("Unable to load proto buffer description file: "
                    + DEFAULT_DESC_FILE);
        }
        FileDescriptorSet fdSet = FileDescriptorSet.parseFrom(in);
        for (FileDescriptorProto fdp : fdSet.getFileList()) {
            List<FileDescriptor> dependencies = new ArrayList<>(fdp.getDependencyCount());
            for (String dep : fdp.getDependencyList()) {
                dependencies.add(fileDescriptorMap.get(dep));
            }
            FileDescriptor fd = FileDescriptor.buildFrom(fdp,
                    dependencies.toArray(new FileDescriptor[dependencies.size()]));
            LOG.debug("Add new file descriptor: " + fd.getFullName());
            fileDescriptorMap.put(fd.getFullName(), fd);

            for (Descriptor messageType : fd.getMessageTypes()) {
                LOG.debug("Add new message type: " + messageType.getFullName());
                typeMap.put(messageType.getFullName(), messageType);
            }
            for (EnumDescriptor ed : fd.getEnumTypes()) {
                LOG.debug("Add new enum descriptor: " + ed.getFullName());
                enumMap.put(ed.getFullName(), ed);
            }
        }

        Descriptor dp = typeMap.get("com.ctrip.ferriswheel.proto.UnionValue");
        System.out.println("UnionValue -> " + dp);
        Map<Integer, FieldDescriptor> fields = new HashMap<>();
        for (FieldDescriptor fd : dp.getFields()) {
            System.out.println(fd);
            fields.put(fd.getNumber(), fd);
        }
        DynamicMessage.Builder builder = DynamicMessage.newBuilder(dp);
        builder.setField(fields.get(1), "NOW()"); // formula string = ""
        builder.setField(fields.get(5), Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build()); // date
        DynamicMessage dmsg = builder.build();
        System.out.println(JsonFormat.printer().print(dmsg));

        UnionValue uv = UnionValue.parseFrom(dmsg.toByteArray());
        System.out.println(uv);
        Value v = PbHelper.toValue(uv);
        System.out.println(v);

        dmsg = DynamicMessage.newBuilder(dp).mergeFrom(dmsg.toByteArray()).build();
        System.out.println(dmsg);
    }

    public static void main(String[] args) throws IOException, DescriptorValidationException {
        new DynamicCodec();
    }
}
