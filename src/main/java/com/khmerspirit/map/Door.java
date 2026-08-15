package com.khmerspirit.map;

public class Door {

    private final int column;
    private final int row;
    private final String fromRoomId;
    private final String toRoomId;

    public Door(int column, int row, String fromRoomId, String toRoomId) {
        this.column = column;
        this.row = row;
        this.fromRoomId = fromRoomId;
        this.toRoomId = toRoomId;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public String getFromRoomId() {
        return fromRoomId;
    }

    public String getToRoomId() {
        return toRoomId;
    }
}
