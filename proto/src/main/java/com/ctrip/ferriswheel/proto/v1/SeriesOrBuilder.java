// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: com/ctrip/ferriswheel/proto/workbook.proto

package com.ctrip.ferriswheel.proto.v1;

public interface SeriesOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ferriswheel.v1.Series)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.ferriswheel.v1.UnionValue name = 1;</code>
   */
  boolean hasName();
  /**
   * <code>.ferriswheel.v1.UnionValue name = 1;</code>
   */
  com.ctrip.ferriswheel.proto.v1.UnionValue getName();
  /**
   * <code>.ferriswheel.v1.UnionValue name = 1;</code>
   */
  com.ctrip.ferriswheel.proto.v1.UnionValueOrBuilder getNameOrBuilder();

  /**
   * <code>.ferriswheel.v1.UnionValue x_values = 2;</code>
   */
  boolean hasXValues();
  /**
   * <code>.ferriswheel.v1.UnionValue x_values = 2;</code>
   */
  com.ctrip.ferriswheel.proto.v1.UnionValue getXValues();
  /**
   * <code>.ferriswheel.v1.UnionValue x_values = 2;</code>
   */
  com.ctrip.ferriswheel.proto.v1.UnionValueOrBuilder getXValuesOrBuilder();

  /**
   * <code>.ferriswheel.v1.UnionValue y_values = 3;</code>
   */
  boolean hasYValues();
  /**
   * <code>.ferriswheel.v1.UnionValue y_values = 3;</code>
   */
  com.ctrip.ferriswheel.proto.v1.UnionValue getYValues();
  /**
   * <code>.ferriswheel.v1.UnionValue y_values = 3;</code>
   */
  com.ctrip.ferriswheel.proto.v1.UnionValueOrBuilder getYValuesOrBuilder();

  /**
   * <code>.ferriswheel.v1.UnionValue z_values = 4;</code>
   */
  boolean hasZValues();
  /**
   * <code>.ferriswheel.v1.UnionValue z_values = 4;</code>
   */
  com.ctrip.ferriswheel.proto.v1.UnionValue getZValues();
  /**
   * <code>.ferriswheel.v1.UnionValue z_values = 4;</code>
   */
  com.ctrip.ferriswheel.proto.v1.UnionValueOrBuilder getZValuesOrBuilder();
}