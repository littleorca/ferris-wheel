// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: com/ctrip/ferriswheel/proto/action.proto

package com.ctrip.ferriswheel.proto.v1;

public interface AddFormOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ferriswheel.v1.AddForm)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string sheet_name = 1;</code>
   */
  java.lang.String getSheetName();
  /**
   * <code>string sheet_name = 1;</code>
   */
  com.google.protobuf.ByteString
      getSheetNameBytes();

  /**
   * <code>.ferriswheel.v1.Form form = 2;</code>
   */
  boolean hasForm();
  /**
   * <code>.ferriswheel.v1.Form form = 2;</code>
   */
  com.ctrip.ferriswheel.proto.v1.Form getForm();
  /**
   * <code>.ferriswheel.v1.Form form = 2;</code>
   */
  com.ctrip.ferriswheel.proto.v1.FormOrBuilder getFormOrBuilder();
}
