package ru.serge2nd.octopussy.dataenv;

public class Queries {

    public static final String RICH_VIEW = "SELECT\n" +
            "  CAST(1 AS BIT) AS C1,\n" +
            "  CAST(127 AS TINYINT) AS C2,\n" +
            "  CAST(32767 AS SMALLINT) AS C3,\n" +
            "  CAST(2147483647 AS INT) AS C4,\n" +
            "  CAST(9223372036854775807 AS BIGINT) AS C5,\n" +
            "  CAST(9223372036854775807.65 AS DECIMAL(21, 2)) AS C6,\n" +
            "  CAST(999.9999 AS REAL) AS C7,\n" +
            "  CAST(99999999.99999999 AS DOUBLE) AS C8,\n" +
            "  CAST('15:30:16' AS TIME) AS C9,\n" +
            "  CAST('2018-09-24' AS DATE) AS C10,\n" +
            "  CAST('2019-01-01T01:45:38' AS TIMESTAMP) AS C11,\n" +
            "  CAST('2020-03-08T08:07:00+03' AS TIMESTAMP WITH TIME ZONE) AS C12,\n" +
            "  CAST(X'ABCDEF' AS VARBINARY) AS C13,\n" +
            "  CAST('abcdef' AS VARCHAR) AS C14,\n" +
            "  CAST(X'ABCDEF' AS BLOB) AS C15,\n" +
            "  CAST('abcdef' AS CLOB) AS C16\n" +
            "UNION\n" +
            "SELECT\n" +
            "  CAST(0 AS BIT) AS C1,\n" +
            "  CAST(-128 AS TINYINT) AS C2,\n" +
            "  CAST(-32768 AS SMALLINT) AS C3,\n" +
            "  CAST(-2147483648 AS INT) AS C4,\n" +
            "  CAST(-9223372036854775808 AS BIGINT) AS C5,\n" +
            "  CAST(-9223372036854775807.65 AS DECIMAL(21, 2)) AS C6,\n" +
            "  CAST(-999.9999 AS REAL) AS C7,\n" +
            "  CAST(-99999999.99999999 AS DOUBLE) AS C8,\n" +
            "  CAST('21:30:16' AS TIME) AS C9,\n" +
            "  CAST('2020-03-22' AS DATE) AS C10,\n" +
            "  CAST('2019-01-09T15:08:00' AS TIMESTAMP) AS C11,\n" +
            "  CAST('2018-06-07T10:20:30+03' AS TIMESTAMP WITH TIME ZONE) AS C12,\n" +
            "  CAST(X'FEDCBA' AS VARBINARY) AS C13,\n" +
            "  CAST('fedcba' AS VARCHAR) AS C14,\n" +
            "  CAST(X'FEDCBA' AS BLOB) AS C15,\n" +
            "  CAST('fedcba' AS CLOB) AS C16";
}
