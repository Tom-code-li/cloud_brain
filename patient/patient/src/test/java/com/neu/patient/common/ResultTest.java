package com.neu.patient.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void testOkWithData() {
        Result<String> result = Result.ok("测试数据");

        assertTrue(result.isSuccess());
        assertEquals("操作成功", result.getMessage());
        assertEquals("测试数据", result.getData());
    }

    @Test
    void testOkWithMessageAndData() {
        Result<Integer> result = Result.ok("自定义消息", 42);

        assertTrue(result.isSuccess());
        assertEquals("自定义消息", result.getMessage());
        assertEquals(42, result.getData());
    }

    @Test
    void testOkWithNullData() {
        Result<Object> result = Result.ok(null);

        assertTrue(result.isSuccess());
        assertNull(result.getData());
    }

    @Test
    void testFail() {
        Result<Object> result = Result.fail("出错了");

        assertFalse(result.isSuccess());
        assertEquals("出错了", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testFailWithEmptyMessage() {
        Result<Object> result = Result.fail("");

        assertFalse(result.isSuccess());
        assertEquals("", result.getMessage());
    }

    @Test
    void testSettersAndGetters() {
        Result<String> result = new Result<>();
        result.setSuccess(false);
        result.setMessage("错误信息");
        result.setData("错误数据");

        assertFalse(result.isSuccess());
        assertEquals("错误信息", result.getMessage());
        assertEquals("错误数据", result.getData());
    }

    @Test
    void testOkGenericType() {
        Result<Object> result = Result.ok(new Object());
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }
}
