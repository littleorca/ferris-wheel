// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: com/ctrip/ferriswheel/proto/action.proto

package com.ctrip.ferriswheel.proto.v1;

/**
 * <pre>
 * used for internal cell update (both value and formula)
 * </pre>
 *
 * Protobuf type {@code ferriswheel.v1.RefreshCellValue}
 */
public  final class RefreshCellValue extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:ferriswheel.v1.RefreshCellValue)
    RefreshCellValueOrBuilder {
private static final long serialVersionUID = 0L;
  // Use RefreshCellValue.newBuilder() to construct.
  private RefreshCellValue(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private RefreshCellValue() {
    sheetName_ = "";
    tableName_ = "";
    rowIndex_ = 0;
    columnIndex_ = 0;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private RefreshCellValue(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    int mutable_bitField0_ = 0;
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 10: {
            java.lang.String s = input.readStringRequireUtf8();

            sheetName_ = s;
            break;
          }
          case 18: {
            java.lang.String s = input.readStringRequireUtf8();

            tableName_ = s;
            break;
          }
          case 24: {

            rowIndex_ = input.readInt32();
            break;
          }
          case 32: {

            columnIndex_ = input.readInt32();
            break;
          }
          case 42: {
            com.ctrip.ferriswheel.proto.v1.UnionValue.Builder subBuilder = null;
            if (value_ != null) {
              subBuilder = value_.toBuilder();
            }
            value_ = input.readMessage(com.ctrip.ferriswheel.proto.v1.UnionValue.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(value_);
              value_ = subBuilder.buildPartial();
            }

            break;
          }
          default: {
            if (!parseUnknownFieldProto3(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.ctrip.ferriswheel.proto.v1.ActionOuterClass.internal_static_ferriswheel_v1_RefreshCellValue_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.ctrip.ferriswheel.proto.v1.ActionOuterClass.internal_static_ferriswheel_v1_RefreshCellValue_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.ctrip.ferriswheel.proto.v1.RefreshCellValue.class, com.ctrip.ferriswheel.proto.v1.RefreshCellValue.Builder.class);
  }

  public static final int SHEET_NAME_FIELD_NUMBER = 1;
  private volatile java.lang.Object sheetName_;
  /**
   * <code>string sheet_name = 1;</code>
   */
  public java.lang.String getSheetName() {
    java.lang.Object ref = sheetName_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      sheetName_ = s;
      return s;
    }
  }
  /**
   * <code>string sheet_name = 1;</code>
   */
  public com.google.protobuf.ByteString
      getSheetNameBytes() {
    java.lang.Object ref = sheetName_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      sheetName_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int TABLE_NAME_FIELD_NUMBER = 2;
  private volatile java.lang.Object tableName_;
  /**
   * <code>string table_name = 2;</code>
   */
  public java.lang.String getTableName() {
    java.lang.Object ref = tableName_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      tableName_ = s;
      return s;
    }
  }
  /**
   * <code>string table_name = 2;</code>
   */
  public com.google.protobuf.ByteString
      getTableNameBytes() {
    java.lang.Object ref = tableName_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      tableName_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int ROW_INDEX_FIELD_NUMBER = 3;
  private int rowIndex_;
  /**
   * <code>int32 row_index = 3;</code>
   */
  public int getRowIndex() {
    return rowIndex_;
  }

  public static final int COLUMN_INDEX_FIELD_NUMBER = 4;
  private int columnIndex_;
  /**
   * <code>int32 column_index = 4;</code>
   */
  public int getColumnIndex() {
    return columnIndex_;
  }

  public static final int VALUE_FIELD_NUMBER = 5;
  private com.ctrip.ferriswheel.proto.v1.UnionValue value_;
  /**
   * <code>.ferriswheel.v1.UnionValue value = 5;</code>
   */
  public boolean hasValue() {
    return value_ != null;
  }
  /**
   * <code>.ferriswheel.v1.UnionValue value = 5;</code>
   */
  public com.ctrip.ferriswheel.proto.v1.UnionValue getValue() {
    return value_ == null ? com.ctrip.ferriswheel.proto.v1.UnionValue.getDefaultInstance() : value_;
  }
  /**
   * <code>.ferriswheel.v1.UnionValue value = 5;</code>
   */
  public com.ctrip.ferriswheel.proto.v1.UnionValueOrBuilder getValueOrBuilder() {
    return getValue();
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!getSheetNameBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, sheetName_);
    }
    if (!getTableNameBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, tableName_);
    }
    if (rowIndex_ != 0) {
      output.writeInt32(3, rowIndex_);
    }
    if (columnIndex_ != 0) {
      output.writeInt32(4, columnIndex_);
    }
    if (value_ != null) {
      output.writeMessage(5, getValue());
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getSheetNameBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, sheetName_);
    }
    if (!getTableNameBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, tableName_);
    }
    if (rowIndex_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(3, rowIndex_);
    }
    if (columnIndex_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(4, columnIndex_);
    }
    if (value_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(5, getValue());
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof com.ctrip.ferriswheel.proto.v1.RefreshCellValue)) {
      return super.equals(obj);
    }
    com.ctrip.ferriswheel.proto.v1.RefreshCellValue other = (com.ctrip.ferriswheel.proto.v1.RefreshCellValue) obj;

    boolean result = true;
    result = result && getSheetName()
        .equals(other.getSheetName());
    result = result && getTableName()
        .equals(other.getTableName());
    result = result && (getRowIndex()
        == other.getRowIndex());
    result = result && (getColumnIndex()
        == other.getColumnIndex());
    result = result && (hasValue() == other.hasValue());
    if (hasValue()) {
      result = result && getValue()
          .equals(other.getValue());
    }
    result = result && unknownFields.equals(other.unknownFields);
    return result;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + SHEET_NAME_FIELD_NUMBER;
    hash = (53 * hash) + getSheetName().hashCode();
    hash = (37 * hash) + TABLE_NAME_FIELD_NUMBER;
    hash = (53 * hash) + getTableName().hashCode();
    hash = (37 * hash) + ROW_INDEX_FIELD_NUMBER;
    hash = (53 * hash) + getRowIndex();
    hash = (37 * hash) + COLUMN_INDEX_FIELD_NUMBER;
    hash = (53 * hash) + getColumnIndex();
    if (hasValue()) {
      hash = (37 * hash) + VALUE_FIELD_NUMBER;
      hash = (53 * hash) + getValue().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.ctrip.ferriswheel.proto.v1.RefreshCellValue parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.ctrip.ferriswheel.proto.v1.RefreshCellValue parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.ctrip.ferriswheel.proto.v1.RefreshCellValue parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.ctrip.ferriswheel.proto.v1.RefreshCellValue parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.ctrip.ferriswheel.proto.v1.RefreshCellValue parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.ctrip.ferriswheel.proto.v1.RefreshCellValue parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.ctrip.ferriswheel.proto.v1.RefreshCellValue parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.ctrip.ferriswheel.proto.v1.RefreshCellValue parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.ctrip.ferriswheel.proto.v1.RefreshCellValue parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.ctrip.ferriswheel.proto.v1.RefreshCellValue parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.ctrip.ferriswheel.proto.v1.RefreshCellValue parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.ctrip.ferriswheel.proto.v1.RefreshCellValue parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(com.ctrip.ferriswheel.proto.v1.RefreshCellValue prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * <pre>
   * used for internal cell update (both value and formula)
   * </pre>
   *
   * Protobuf type {@code ferriswheel.v1.RefreshCellValue}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:ferriswheel.v1.RefreshCellValue)
      com.ctrip.ferriswheel.proto.v1.RefreshCellValueOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.ctrip.ferriswheel.proto.v1.ActionOuterClass.internal_static_ferriswheel_v1_RefreshCellValue_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.ctrip.ferriswheel.proto.v1.ActionOuterClass.internal_static_ferriswheel_v1_RefreshCellValue_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.ctrip.ferriswheel.proto.v1.RefreshCellValue.class, com.ctrip.ferriswheel.proto.v1.RefreshCellValue.Builder.class);
    }

    // Construct using com.ctrip.ferriswheel.proto.v1.RefreshCellValue.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      sheetName_ = "";

      tableName_ = "";

      rowIndex_ = 0;

      columnIndex_ = 0;

      if (valueBuilder_ == null) {
        value_ = null;
      } else {
        value_ = null;
        valueBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.ctrip.ferriswheel.proto.v1.ActionOuterClass.internal_static_ferriswheel_v1_RefreshCellValue_descriptor;
    }

    @java.lang.Override
    public com.ctrip.ferriswheel.proto.v1.RefreshCellValue getDefaultInstanceForType() {
      return com.ctrip.ferriswheel.proto.v1.RefreshCellValue.getDefaultInstance();
    }

    @java.lang.Override
    public com.ctrip.ferriswheel.proto.v1.RefreshCellValue build() {
      com.ctrip.ferriswheel.proto.v1.RefreshCellValue result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.ctrip.ferriswheel.proto.v1.RefreshCellValue buildPartial() {
      com.ctrip.ferriswheel.proto.v1.RefreshCellValue result = new com.ctrip.ferriswheel.proto.v1.RefreshCellValue(this);
      result.sheetName_ = sheetName_;
      result.tableName_ = tableName_;
      result.rowIndex_ = rowIndex_;
      result.columnIndex_ = columnIndex_;
      if (valueBuilder_ == null) {
        result.value_ = value_;
      } else {
        result.value_ = valueBuilder_.build();
      }
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return (Builder) super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return (Builder) super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return (Builder) super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return (Builder) super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return (Builder) super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return (Builder) super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof com.ctrip.ferriswheel.proto.v1.RefreshCellValue) {
        return mergeFrom((com.ctrip.ferriswheel.proto.v1.RefreshCellValue)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.ctrip.ferriswheel.proto.v1.RefreshCellValue other) {
      if (other == com.ctrip.ferriswheel.proto.v1.RefreshCellValue.getDefaultInstance()) return this;
      if (!other.getSheetName().isEmpty()) {
        sheetName_ = other.sheetName_;
        onChanged();
      }
      if (!other.getTableName().isEmpty()) {
        tableName_ = other.tableName_;
        onChanged();
      }
      if (other.getRowIndex() != 0) {
        setRowIndex(other.getRowIndex());
      }
      if (other.getColumnIndex() != 0) {
        setColumnIndex(other.getColumnIndex());
      }
      if (other.hasValue()) {
        mergeValue(other.getValue());
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      com.ctrip.ferriswheel.proto.v1.RefreshCellValue parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (com.ctrip.ferriswheel.proto.v1.RefreshCellValue) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object sheetName_ = "";
    /**
     * <code>string sheet_name = 1;</code>
     */
    public java.lang.String getSheetName() {
      java.lang.Object ref = sheetName_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        sheetName_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string sheet_name = 1;</code>
     */
    public com.google.protobuf.ByteString
        getSheetNameBytes() {
      java.lang.Object ref = sheetName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        sheetName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string sheet_name = 1;</code>
     */
    public Builder setSheetName(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      sheetName_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string sheet_name = 1;</code>
     */
    public Builder clearSheetName() {
      
      sheetName_ = getDefaultInstance().getSheetName();
      onChanged();
      return this;
    }
    /**
     * <code>string sheet_name = 1;</code>
     */
    public Builder setSheetNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      sheetName_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object tableName_ = "";
    /**
     * <code>string table_name = 2;</code>
     */
    public java.lang.String getTableName() {
      java.lang.Object ref = tableName_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        tableName_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string table_name = 2;</code>
     */
    public com.google.protobuf.ByteString
        getTableNameBytes() {
      java.lang.Object ref = tableName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        tableName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string table_name = 2;</code>
     */
    public Builder setTableName(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      tableName_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string table_name = 2;</code>
     */
    public Builder clearTableName() {
      
      tableName_ = getDefaultInstance().getTableName();
      onChanged();
      return this;
    }
    /**
     * <code>string table_name = 2;</code>
     */
    public Builder setTableNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      tableName_ = value;
      onChanged();
      return this;
    }

    private int rowIndex_ ;
    /**
     * <code>int32 row_index = 3;</code>
     */
    public int getRowIndex() {
      return rowIndex_;
    }
    /**
     * <code>int32 row_index = 3;</code>
     */
    public Builder setRowIndex(int value) {
      
      rowIndex_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 row_index = 3;</code>
     */
    public Builder clearRowIndex() {
      
      rowIndex_ = 0;
      onChanged();
      return this;
    }

    private int columnIndex_ ;
    /**
     * <code>int32 column_index = 4;</code>
     */
    public int getColumnIndex() {
      return columnIndex_;
    }
    /**
     * <code>int32 column_index = 4;</code>
     */
    public Builder setColumnIndex(int value) {
      
      columnIndex_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 column_index = 4;</code>
     */
    public Builder clearColumnIndex() {
      
      columnIndex_ = 0;
      onChanged();
      return this;
    }

    private com.ctrip.ferriswheel.proto.v1.UnionValue value_ = null;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.ctrip.ferriswheel.proto.v1.UnionValue, com.ctrip.ferriswheel.proto.v1.UnionValue.Builder, com.ctrip.ferriswheel.proto.v1.UnionValueOrBuilder> valueBuilder_;
    /**
     * <code>.ferriswheel.v1.UnionValue value = 5;</code>
     */
    public boolean hasValue() {
      return valueBuilder_ != null || value_ != null;
    }
    /**
     * <code>.ferriswheel.v1.UnionValue value = 5;</code>
     */
    public com.ctrip.ferriswheel.proto.v1.UnionValue getValue() {
      if (valueBuilder_ == null) {
        return value_ == null ? com.ctrip.ferriswheel.proto.v1.UnionValue.getDefaultInstance() : value_;
      } else {
        return valueBuilder_.getMessage();
      }
    }
    /**
     * <code>.ferriswheel.v1.UnionValue value = 5;</code>
     */
    public Builder setValue(com.ctrip.ferriswheel.proto.v1.UnionValue value) {
      if (valueBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        value_ = value;
        onChanged();
      } else {
        valueBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.ferriswheel.v1.UnionValue value = 5;</code>
     */
    public Builder setValue(
        com.ctrip.ferriswheel.proto.v1.UnionValue.Builder builderForValue) {
      if (valueBuilder_ == null) {
        value_ = builderForValue.build();
        onChanged();
      } else {
        valueBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.ferriswheel.v1.UnionValue value = 5;</code>
     */
    public Builder mergeValue(com.ctrip.ferriswheel.proto.v1.UnionValue value) {
      if (valueBuilder_ == null) {
        if (value_ != null) {
          value_ =
            com.ctrip.ferriswheel.proto.v1.UnionValue.newBuilder(value_).mergeFrom(value).buildPartial();
        } else {
          value_ = value;
        }
        onChanged();
      } else {
        valueBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.ferriswheel.v1.UnionValue value = 5;</code>
     */
    public Builder clearValue() {
      if (valueBuilder_ == null) {
        value_ = null;
        onChanged();
      } else {
        value_ = null;
        valueBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.ferriswheel.v1.UnionValue value = 5;</code>
     */
    public com.ctrip.ferriswheel.proto.v1.UnionValue.Builder getValueBuilder() {
      
      onChanged();
      return getValueFieldBuilder().getBuilder();
    }
    /**
     * <code>.ferriswheel.v1.UnionValue value = 5;</code>
     */
    public com.ctrip.ferriswheel.proto.v1.UnionValueOrBuilder getValueOrBuilder() {
      if (valueBuilder_ != null) {
        return valueBuilder_.getMessageOrBuilder();
      } else {
        return value_ == null ?
            com.ctrip.ferriswheel.proto.v1.UnionValue.getDefaultInstance() : value_;
      }
    }
    /**
     * <code>.ferriswheel.v1.UnionValue value = 5;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.ctrip.ferriswheel.proto.v1.UnionValue, com.ctrip.ferriswheel.proto.v1.UnionValue.Builder, com.ctrip.ferriswheel.proto.v1.UnionValueOrBuilder> 
        getValueFieldBuilder() {
      if (valueBuilder_ == null) {
        valueBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.ctrip.ferriswheel.proto.v1.UnionValue, com.ctrip.ferriswheel.proto.v1.UnionValue.Builder, com.ctrip.ferriswheel.proto.v1.UnionValueOrBuilder>(
                getValue(),
                getParentForChildren(),
                isClean());
        value_ = null;
      }
      return valueBuilder_;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:ferriswheel.v1.RefreshCellValue)
  }

  // @@protoc_insertion_point(class_scope:ferriswheel.v1.RefreshCellValue)
  private static final com.ctrip.ferriswheel.proto.v1.RefreshCellValue DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.ctrip.ferriswheel.proto.v1.RefreshCellValue();
  }

  public static com.ctrip.ferriswheel.proto.v1.RefreshCellValue getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<RefreshCellValue>
      PARSER = new com.google.protobuf.AbstractParser<RefreshCellValue>() {
    @java.lang.Override
    public RefreshCellValue parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new RefreshCellValue(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<RefreshCellValue> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<RefreshCellValue> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.ctrip.ferriswheel.proto.v1.RefreshCellValue getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
