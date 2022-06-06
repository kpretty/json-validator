import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 * @author wjun
 * @date 2022/6/6 16:55
 * @email wjunjobs@outlook.com
 * @describe 校验json格式是否合法
 */
public class JsonValidator {
    private CharacterIterator it;
    private char c;
    private int col;

    public JsonValidator() {
    }

    /**
     * 验证一个字符串是否是合法的JSON串
     *
     * @param input 要验证的字符串
     * @return true-合法 ，false-非法
     */
    public boolean validate(String input) {
        input = input.trim();
        return valid(input);
    }

    private boolean valid(String input) {
        if ("".equals(input)) return true;

        boolean ret = true;
        it = new StringCharacterIterator(input);
        c = it.first();
        col = 1;
        if (!value()) {
            ret = error("value", 1);
        } else {
            skipWhiteSpace();
            if (c != CharacterIterator.DONE) {
                ret = error("end", col);
            }
        }

        return ret;
    }

    private boolean value() {
        return literal("true") || literal("false") || literal("null") || string() || number() || object() || array();
    }

    private boolean literal(String text) {
        CharacterIterator ci = new StringCharacterIterator(text);
        char t = ci.first();
        if (c != t) return false;

        int start = col;
        boolean ret = true;
        for (t = ci.next(); t != CharacterIterator.DONE; t = ci.next()) {
            if (t != nextCharacter()) {
                ret = false;
                break;
            }
        }
        nextCharacter();
        if (!ret) error("literal " + text, start);
        return ret;
    }

    private boolean array() {
        return aggregate('[', ']', false);
    }

    private boolean object() {
        return aggregate('{', '}', true);
    }

    private boolean aggregate(char entryCharacter, char exitCharacter, boolean prefix) {
        if (c != entryCharacter) return false;
        nextCharacter();
        skipWhiteSpace();
        if (c == exitCharacter) {
            nextCharacter();
            return true;
        }

        for (; ; ) {
            if (prefix) {
                int start = col;
                if (!string()) return error("string", start);
                skipWhiteSpace();
                if (c != ':') return error("colon", col);
                nextCharacter();
                skipWhiteSpace();
            }
            if (value()) {
                skipWhiteSpace();
                if (c == ',') {
                    nextCharacter();
                } else if (c == exitCharacter) {
                    break;
                } else {
                    return error("comma or " + exitCharacter, col);
                }
            } else {
                return error("value", col);
            }
            skipWhiteSpace();
        }

        nextCharacter();
        return true;
    }

    private boolean number() {
        if (!Character.isDigit(c) && c != '-') return false;
        int start = col;
        if (c == '-') nextCharacter();
        if (c == '0') {
            nextCharacter();
        } else if (Character.isDigit(c)) {
            while (Character.isDigit(c))
                nextCharacter();
        } else {
            return error("number", start);
        }
        if (c == '.') {
            nextCharacter();
            if (Character.isDigit(c)) {
                while (Character.isDigit(c))
                    nextCharacter();
            } else {
                return error("number", start);
            }
        }
        if (c == 'e' || c == 'E') {
            nextCharacter();
            if (c == '+' || c == '-') {
                nextCharacter();
            }
            if (Character.isDigit(c)) {
                while (Character.isDigit(c))
                    nextCharacter();
            } else {
                return error("number", start);
            }
        }
        return true;
    }

    private boolean string() {
        if (c != '"') return false;

        int start = col;
        boolean escaped = false;
        for (nextCharacter(); c != CharacterIterator.DONE; nextCharacter()) {
            if (!escaped && c == '\\') {
                escaped = true;
            } else if (escaped) {
                if (!escape()) {
                    return false;
                }
                escaped = false;
            } else if (c == '"') {
                nextCharacter();
                return true;
            }
        }
        return error("quoted string", start);
    }

    private boolean escape() {
        int start = col - 1;
        if (" \\\"/bfnrtu".indexOf(c) < 0) {
            return error("escape sequence  \\\",\\\\,\\/,\\b,\\f,\\n,\\r,\\t  or  \\uxxxx ", start);
        }
        if (c == 'u') {
            if (isHex(nextCharacter()) || isHex(nextCharacter()) || isHex(nextCharacter())
                    || isHex(nextCharacter())) {
                return error("unicode escape sequence  \\uxxxx ", start);
            }
        }
        return true;
    }

    private boolean isHex(char d) {
        return "0123456789abcdefABCDEF".indexOf(c) < 0;
    }

    private char nextCharacter() {
        c = it.next();
        ++col;
        return c;
    }

    private void skipWhiteSpace() {
        while (Character.isWhitespace(c)) {
            nextCharacter();
        }
    }

    private boolean error(String type, int col) {
//        System.out.printf("type: %s, col: %s%s", type, col, System.getProperty("line.separator"));
        return false;
    }

    public static void main(String[] args) {
        String jsonStr = "{\n" +
                "    \"status\": 0,\n" +
                "    \"message\": \"\",\n" +
                "    \"data\": {\n" +
                "        \"search_data\": [\n" +
                "            {\n" +
                "                \"elements\": [\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"奈良市\",\n" +
                "                        \"url\": \"/scenic/3/10052/\",\n" +
                "                        \"wish_to_go_count\": 328,\n" +
                "                        \"name_orig\": \"奈良市\",\n" +
                "                        \"visited_count\": 1958,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 34.685087,\n" +
                "                            \"lng\": 135.805\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"rating_users\": 0,\n" +
                "                        \"name_zh\": \"奈良市\",\n" +
                "                        \"name_en\": \"Nara\",\n" +
                "                        \"type\": 3,\n" +
                "                        \"id\": 10052,\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/city.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"小樽市\",\n" +
                "                        \"url\": \"/scenic/3/26772/\",\n" +
                "                        \"wish_to_go_count\": 266,\n" +
                "                        \"name_orig\": \"小樽市\",\n" +
                "                        \"visited_count\": 954,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 43.190717,\n" +
                "                            \"lng\": 140.994662\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"rating_users\": 0,\n" +
                "                        \"name_zh\": \"小樽市\",\n" +
                "                        \"name_en\": \"Otaru\",\n" +
                "                        \"type\": 3,\n" +
                "                        \"id\": 26772,\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/city.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"槟城\",\n" +
                "                        \"url\": \"/scenic/2/8257/\",\n" +
                "                        \"wish_to_go_count\": 93,\n" +
                "                        \"name_orig\": \"槟城\",\n" +
                "                        \"visited_count\": 849,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 5.414167,\n" +
                "                            \"lng\": 100.328759\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"rating_users\": 0,\n" +
                "                        \"name_zh\": \"槟城\",\n" +
                "                        \"name_en\": \"Penang\",\n" +
                "                        \"type\": 2,\n" +
                "                        \"id\": 8257,\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/province.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"墨尔本\",\n" +
                "                        \"url\": \"/scenic/3/47810/\",\n" +
                "                        \"wish_to_go_count\": 2927,\n" +
                "                        \"name_orig\": \"墨尔本\",\n" +
                "                        \"visited_count\": 2112,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": -37.814216,\n" +
                "                            \"lng\": 144.963231\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"rating_users\": 0,\n" +
                "                        \"name_zh\": \"墨尔本\",\n" +
                "                        \"name_en\": \"Melbourne\",\n" +
                "                        \"type\": 3,\n" +
                "                        \"id\": 47810,\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/city.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"广岛县\",\n" +
                "                        \"url\": \"/scenic/2/9474/\",\n" +
                "                        \"wish_to_go_count\": 36,\n" +
                "                        \"name_orig\": \"广岛县\",\n" +
                "                        \"visited_count\": 160,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 34.39656,\n" +
                "                            \"lng\": 132.459622\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"rating_users\": 0,\n" +
                "                        \"name_zh\": \"广岛县\",\n" +
                "                        \"name_en\": \"Hiroshima Prefecture\",\n" +
                "                        \"type\": 2,\n" +
                "                        \"id\": 9474,\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/province.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"泰国\",\n" +
                "                        \"url\": \"/scenic/1/3649/\",\n" +
                "                        \"wish_to_go_count\": 22131,\n" +
                "                        \"name_orig\": \"泰国\",\n" +
                "                        \"visited_count\": 22298,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 15.870032,\n" +
                "                            \"lng\": 100.992541\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/country.png\",\n" +
                "                        \"type\": 1,\n" +
                "                        \"id\": \"TH\",\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"rating_users\": 0\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"芬兰\",\n" +
                "                        \"url\": \"/scenic/1/3613/\",\n" +
                "                        \"wish_to_go_count\": 665,\n" +
                "                        \"name_orig\": \"芬兰\",\n" +
                "                        \"visited_count\": 1058,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 61.92411,\n" +
                "                            \"lng\": 25.748151\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/country.png\",\n" +
                "                        \"type\": 1,\n" +
                "                        \"id\": \"FI\",\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"rating_users\": 0\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"美国\",\n" +
                "                        \"url\": \"/scenic/1/3803/\",\n" +
                "                        \"wish_to_go_count\": 8828,\n" +
                "                        \"name_orig\": \"美国\",\n" +
                "                        \"visited_count\": 12967,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 37.09024,\n" +
                "                            \"lng\": -95.712891\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/country.png\",\n" +
                "                        \"type\": 1,\n" +
                "                        \"id\": \"US\",\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"rating_users\": 0\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"马来西亚\",\n" +
                "                        \"url\": \"/scenic/1/3676/\",\n" +
                "                        \"wish_to_go_count\": 6339,\n" +
                "                        \"name_orig\": \"马来西亚\",\n" +
                "                        \"visited_count\": 9533,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 4.210484,\n" +
                "                            \"lng\": 101.975766\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/country.png\",\n" +
                "                        \"type\": 1,\n" +
                "                        \"id\": \"MY\",\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"rating_users\": 0\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"意大利\",\n" +
                "                        \"url\": \"/scenic/1/3720/\",\n" +
                "                        \"wish_to_go_count\": 7689,\n" +
                "                        \"name_orig\": \"意大利\",\n" +
                "                        \"visited_count\": 6508,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 41.87194,\n" +
                "                            \"lng\": 12.56738\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/country.png\",\n" +
                "                        \"type\": 1,\n" +
                "                        \"id\": \"IT\",\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"rating_users\": 0\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"新加坡\",\n" +
                "                        \"url\": \"/scenic/1/3589/\",\n" +
                "                        \"wish_to_go_count\": 5847,\n" +
                "                        \"name_orig\": \"新加坡\",\n" +
                "                        \"visited_count\": 7835,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 1.352083,\n" +
                "                            \"lng\": 103.819836\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/country.png\",\n" +
                "                        \"type\": 1,\n" +
                "                        \"id\": \"SG\",\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"rating_users\": 0\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"挪威\",\n" +
                "                        \"url\": \"/scenic/1/3258/\",\n" +
                "                        \"wish_to_go_count\": 788,\n" +
                "                        \"name_orig\": \"挪威\",\n" +
                "                        \"visited_count\": 850,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 60.472024,\n" +
                "                            \"lng\": 8.468946\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/country.png\",\n" +
                "                        \"type\": 1,\n" +
                "                        \"id\": \"NO\",\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"rating_users\": 0\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"type\": \"destination\",\n" +
                "                \"title\": \"国外热门目的地\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"elements\": [\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"台湾\",\n" +
                "                        \"url\": \"/scenic/1/3660/\",\n" +
                "                        \"wish_to_go_count\": 37947,\n" +
                "                        \"name_orig\": \"台湾\",\n" +
                "                        \"visited_count\": 15729,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 23.69781,\n" +
                "                            \"lng\": 120.960515\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/country.png\",\n" +
                "                        \"type\": 1,\n" +
                "                        \"id\": \"TW\",\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"rating_users\": 0\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"香港\",\n" +
                "                        \"url\": \"/scenic/1/3814/\",\n" +
                "                        \"wish_to_go_count\": 23495,\n" +
                "                        \"name_orig\": \"香港\",\n" +
                "                        \"visited_count\": 31249,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 22.396428,\n" +
                "                            \"lng\": 114.109497\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/country.png\",\n" +
                "                        \"type\": 1,\n" +
                "                        \"id\": \"HK\",\n" +
                "                        \"has_route_maps\": true,\n" +
                "                        \"rating_users\": 0\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"厦门\",\n" +
                "                        \"url\": \"/scenic/3/65012/\",\n" +
                "                        \"wish_to_go_count\": 29887,\n" +
                "                        \"name_orig\": \"厦门\",\n" +
                "                        \"visited_count\": 26077,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 24.477188,\n" +
                "                            \"lng\": 118.094398\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"rating_users\": 0,\n" +
                "                        \"name_zh\": \"厦门\",\n" +
                "                        \"name_en\": \"Xiamen\",\n" +
                "                        \"type\": 3,\n" +
                "                        \"id\": 65012,\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/city.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"北京\",\n" +
                "                        \"url\": \"/scenic/3/8248/\",\n" +
                "                        \"wish_to_go_count\": 7118,\n" +
                "                        \"name_orig\": \"北京\",\n" +
                "                        \"visited_count\": 53416,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 39.90561,\n" +
                "                            \"lng\": 116.413634\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"rating_users\": 0,\n" +
                "                        \"name_zh\": \"北京\",\n" +
                "                        \"name_en\": \"Beijing\",\n" +
                "                        \"type\": 3,\n" +
                "                        \"id\": 8248,\n" +
                "                        \"has_route_maps\": true,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/city.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"丽江市\",\n" +
                "                        \"url\": \"/scenic/3/65362/\",\n" +
                "                        \"wish_to_go_count\": 27368,\n" +
                "                        \"name_orig\": \"丽江市\",\n" +
                "                        \"visited_count\": 19389,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 26.851553,\n" +
                "                            \"lng\": 100.228931\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"rating_users\": 0,\n" +
                "                        \"name_zh\": \"丽江市\",\n" +
                "                        \"name_en\": \"Lijiang\",\n" +
                "                        \"type\": 3,\n" +
                "                        \"id\": 65362,\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/city.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"成都\",\n" +
                "                        \"url\": \"/scenic/3/14209/\",\n" +
                "                        \"wish_to_go_count\": 14464,\n" +
                "                        \"name_orig\": \"成都\",\n" +
                "                        \"visited_count\": 23484,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 30.569858,\n" +
                "                            \"lng\": 104.069084\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"rating_users\": 0,\n" +
                "                        \"name_zh\": \"成都\",\n" +
                "                        \"name_en\": \"Chengdu\",\n" +
                "                        \"type\": 3,\n" +
                "                        \"id\": 14209,\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/city.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"上海\",\n" +
                "                        \"url\": \"/scenic/3/13961/\",\n" +
                "                        \"wish_to_go_count\": 7601,\n" +
                "                        \"name_orig\": \"上海\",\n" +
                "                        \"visited_count\": 46500,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 31.228402,\n" +
                "                            \"lng\": 121.478143\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"rating_users\": 0,\n" +
                "                        \"name_zh\": \"上海\",\n" +
                "                        \"name_en\": \"Shanghai\",\n" +
                "                        \"type\": 3,\n" +
                "                        \"id\": 13961,\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/city.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"拉萨\",\n" +
                "                        \"url\": \"/scenic/3/5249/\",\n" +
                "                        \"wish_to_go_count\": 15492,\n" +
                "                        \"name_orig\": \"拉萨\",\n" +
                "                        \"visited_count\": 6544,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 29.649671,\n" +
                "                            \"lng\": 91.173526\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"rating_users\": 0,\n" +
                "                        \"name_zh\": \"拉萨\",\n" +
                "                        \"name_en\": \"Lhasa\",\n" +
                "                        \"type\": 3,\n" +
                "                        \"id\": 5249,\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/city.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"大理\",\n" +
                "                        \"url\": \"/scenic/3/43908/\",\n" +
                "                        \"wish_to_go_count\": 10103,\n" +
                "                        \"name_orig\": \"大理\",\n" +
                "                        \"visited_count\": 13291,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 25.603496,\n" +
                "                            \"lng\": 100.268781\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"rating_users\": 0,\n" +
                "                        \"name_zh\": \"大理\",\n" +
                "                        \"name_en\": \"Dali\",\n" +
                "                        \"type\": 3,\n" +
                "                        \"id\": 43908,\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/city.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"rating\": 0,\n" +
                "                        \"name\": \"三亚\",\n" +
                "                        \"url\": \"/scenic/3/65557/\",\n" +
                "                        \"wish_to_go_count\": 8951,\n" +
                "                        \"name_orig\": \"三亚\",\n" +
                "                        \"visited_count\": 11920,\n" +
                "                        \"comments_count\": 0,\n" +
                "                        \"location\": {\n" +
                "                            \"lat\": 18.251176,\n" +
                "                            \"lng\": 109.51604\n" +
                "                        },\n" +
                "                        \"has_experience\": false,\n" +
                "                        \"rating_users\": 0,\n" +
                "                        \"name_zh\": \"三亚\",\n" +
                "                        \"name_en\": \"Sanya\",\n" +
                "                        \"type\": 3,\n" +
                "                        \"id\": 65557,\n" +
                "                        \"has_route_maps\": false,\n" +
                "                        \"icon\": \"http://media.breadtrip.com/images/icons/2/city.png\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"type\": \"destination\",\n" +
                "                \"title\": \"国内热门目的地\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"date_time\": \"2017-09-11 10:52:27.811321\",\n" +
                "        \"elements\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"data\": [\n" +
                "                    [\n" +
                "                        {\n" +
                "                            \"platform\": \"android\",\n" +
                "                            \"image_url\": \"http://photos.breadtrip.com/covers_2017_09_05_d5b225291045767fcfa0508f3d96ae26.jpg?imageView/2/w/960/\",\n" +
                "                            \"html_url\": \"http://www.iqiyi.com/v_19rr8w7drg.html?dummy=&wx_uid2=wxidoG0a9jsIGfq1jln1HtWebMCOKKSQ\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"platform\": \"android\",\n" +
                "                            \"image_url\": \"http://photos.breadtrip.com/covers_2014_12_25_ed43331bac65ee0752f7e56116993b2c.jpg?imageView2/2/w/750/format/jpg/interlace/1/\",\n" +
                "                            \"html_url\": \"http://web.breadtrip.com/mobile/destination/topic/2387718817/\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"platform\": \"android\",\n" +
                "                            \"image_url\": \"http://photos.breadtrip.com/covers_2014_10_29_46217a91ace672787ed1fec4c2011b52.png?imageView2/2/w/750/format/jpg/interlace/1/\",\n" +
                "                            \"html_url\": \"http://web.breadtrip.com/mobile/destination/topic/2387718790/\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"platform\": \"android\",\n" +
                "                            \"image_url\": \"http://photos.breadtrip.com/covers_2015_04_24_59b10571419fb3888224d83365f561e8.jpg?imageView2/2/w/750/format/jpg/interlace/1/\",\n" +
                "                            \"html_url\": \"http://web.breadtrip.com/mobile/destination/topic/2387718734/\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"platform\": \"android\",\n" +
                "                            \"image_url\": \"http://photos.breadtrip.com/covers_2014_12_05_c9aea564f43b673ea6d8dcf9c6c4627b.jpg?imageView2/2/w/750/format/jpg/interlace/1/\",\n" +
                "                            \"html_url\": \"http://web.breadtrip.com/mobile/destination/topic/2387718810/\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"platform\": \"android\",\n" +
                "                            \"image_url\": \"http://photos.breadtrip.com/covers_2016_02_26_51987e2bebba67bad75ccb114dfea7ab.png?imageView2/2/w/750/format/jpg/interlace/1/\",\n" +
                "                            \"html_url\": \"http://web.breadtrip.com/mobile/destination/topic/2387719110/\"\n" +
                "                        }\n" +
                "                    ]\n" +
                "                ],\n" +
                "                \"desc\": \"广告banner\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 11,\n" +
                "                \"data\": [\n" +
                "                    {\n" +
                "                        \"title\": \"每日精选故事\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"desc\": \"\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 10,\n" +
                "                \"data\": [\n" +
                "                    {\n" +
                "                        \"text\": \"halo 雷猴嘛\\n我猜你是个妹纸\\n我猜你是个爱美的妹纸\\n我猜你是个喜欢首饰and爱美的妹纸\\n我猜你是个喜欢手作首饰and爱美的妹纸\\n我猜…你是个亲手做首饰给女票的汉纸!\\n\\n\\n\\n我是个喜欢手工喜欢首饰喜欢发现美的美男纸，来吃我一安利呗!\",\n" +
                "                        \"is_liked\": false,\n" +
                "                        \"index_cover\": \"http://photos.breadtrip.com/photo_d_2016_08_02_dca0659576dfe60382943612e2fff308ff2b1f1d56055e6b29f6fdd317f3ff8c.jpeg?imageView/2/w/960/q/85\",\n" +
                "                        \"poi\": {},\n" +
                "                        \"cover_image_height\": 1206,\n" +
                "                        \"trip_id\": 2387270842,\n" +
                "                        \"index_title\": \"\",\n" +
                "                        \"center_point\": {\n" +
                "                            \"lat\": 0,\n" +
                "                            \"lng\": 0\n" +
                "                        },\n" +
                "                        \"view_count\": 45446,\n" +
                "                        \"location_alias\": \"Dream High梦想社\",\n" +
                "                        \"cover_image_1600\": \"http://photos.breadtrip.com/photo_d_2016_08_02_09931d012facf6186f9c25202983dcaa68efddaccb4cbc0e78a0ded46afb778e.jpeg?imageView/2/w/1384/h/1384/q/85\",\n" +
                "                        \"cover_image_s\": \"http://photos.breadtrip.com/photo_d_2016_08_02_09931d012facf6186f9c25202983dcaa68efddaccb4cbc0e78a0ded46afb778e.jpeg?imageView/1/w/280/h/280/q/75\",\n" +
                "                        \"share_url\": \"btrip/spot/2387867495/\",\n" +
                "                        \"timezone\": \"Asia/Shanghai\",\n" +
                "                        \"date_tour\": \"2016-07-31T20:49:00+08:00\",\n" +
                "                        \"is_hiding_location\": true,\n" +
                "                        \"user\": {\n" +
                "                            \"location_name\": \"广东_广州\",\n" +
                "                            \"name\": \"林酷儿\",\n" +
                "                            \"resident_city_id\": 275,\n" +
                "                            \"mobile\": \"\",\n" +
                "                            \"gender\": 2,\n" +
                "                            \"avatar_m\": \"http://photos.breadtrip.com/avatar_17_c1_1214298920959e94700b4cb370271320c5745e6c.jpg-avatar.m\",\n" +
                "                            \"cover\": \"http://photos.breadtrip.com/default_user_cover_06.jpg-usercover.display\",\n" +
                "                            \"custom_url\": \"\",\n" +
                "                            \"experience\": {\n" +
                "                                \"value\": 101,\n" +
                "                                \"level_info\": {\n" +
                "                                    \"name\": \"\",\n" +
                "                                    \"value\": 2\n" +
                "                                }\n" +
                "                            },\n" +
                "                            \"id\": 2384430044,\n" +
                "                            \"birthday\": \"\",\n" +
                "                            \"country_num\": null,\n" +
                "                            \"avatar_s\": \"http://photos.breadtrip.com/avatar_17_c1_1214298920959e94700b4cb370271320c5745e6c.jpg-avatar.s\",\n" +
                "                            \"country_code\": null,\n" +
                "                            \"email_verified\": false,\n" +
                "                            \"is_hunter\": false,\n" +
                "                            \"cdc2\": false,\n" +
                "                            \"avatar_l\": \"http://photos.breadtrip.com/avatar_17_c1_1214298920959e94700b4cb370271320c5745e6c.jpg-avatar.l\",\n" +
                "                            \"email\": \"\",\n" +
                "                            \"user_desc\": \"\",\n" +
                "                            \"points\": 26\n" +
                "                        },\n" +
                "                        \"spot_id\": 2387867495,\n" +
                "                        \"is_author\": false,\n" +
                "                        \"cover_image_w640\": \"http://photos.breadtrip.com/photo_d_2016_08_02_09931d012facf6186f9c25202983dcaa68efddaccb4cbc0e78a0ded46afb778e.jpeg?imageView/1/w/640/h/480/q/85\",\n" +
                "                        \"region\": {\n" +
                "                            \"primary\": \"\",\n" +
                "                            \"secondary\": \"\"\n" +
                "                        },\n" +
                "                        \"comments_count\": 9,\n" +
                "                        \"cover_image\": \"http://photos.breadtrip.com/photo_d_2016_08_02_09931d012facf6186f9c25202983dcaa68efddaccb4cbc0e78a0ded46afb778e.jpeg?imageView/2/w/960/q/85\",\n" +
                "                        \"cover_image_width\": 1600,\n" +
                "                        \"recommendations_count\": 22\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"desc\": \"\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 10,\n" +
                "                \"data\": [\n" +
                "                    {\n" +
                "                        \"text\": \"挑战极限和心跳，让自己成为一个有趣的人。\\n2016月5月在面包旅行，偶然下兼职做一个猎人哈哈（活动名字上帝之眼），感觉挺酷的。\\n重庆最具特色的，当然是城市里最耀眼的高楼夜景，曾经有一部电影“重庆森林”，故事很多场景，以及城市展现出重庆这座奇葩一样的城市，独具特色话3D话，很多电影都有在重庆取景。\",\n" +
                "                        \"is_liked\": false,\n" +
                "                        \"index_cover\": \"http://photos.breadtrip.com/photo_d_2016_06_24_4bc9f811fa5b900762fb1d55dff164f32066a28d4010bf9f4a4c20dd62634b4d.jpg?imageView/2/w/960/q/85\",\n" +
                "                        \"poi\": {\n" +
                "                            \"tel\": \"023-62872299\",\n" +
                "                            \"currency\": \"CNY\",\n" +
                "                            \"is_nearby\": true,\n" +
                "                            \"timezone\": \"Asia/Chongqing\",\n" +
                "                            \"id\": 2387458620,\n" +
                "                            \"category\": 11,\n" +
                "                            \"recommended_reason\": \"看滚滚江水向东流去，犹如有登高望远\",\n" +
                "                            \"fee\": \"免费\",\n" +
                "                            \"spot_region\": \"重庆\",\n" +
                "                            \"date_added\": \"2014-12-16 10:48:55\",\n" +
                "                            \"time_consuming_max\": 0,\n" +
                "                            \"time_consuming\": null,\n" +
                "                            \"extra1\": \"\",\n" +
                "                            \"recommended\": true,\n" +
                "                            \"location\": {\n" +
                "                                \"lat\": 29.554926,\n" +
                "                                \"lng\": 106.586696\n" +
                "                            },\n" +
                "                            \"opening_time\": \"全天\",\n" +
                "                            \"type\": 5,\n" +
                "                            \"time_consuming_min\": 0,\n" +
                "                            \"website\": \"\",\n" +
                "                            \"description\": \"长江索道起于渝中区长安寺，横跨长江至南岸区上新街。乘坐过江索道，除了可以欣赏两江美景外，还可以从索道上俯瞰洪崖洞、湖广会馆、南滨路等著名景观。\",\n" +
                "                            \"arrival_type\": \"乘105、132、135、153、181、261、0321夜班、476、0491夜班、0492夜班、0493夜班、871、898路等公交车在新华路站下车即到\",\n" +
                "                            \"address\": \"重庆市渝中区新华路153号\",\n" +
                "                            \"verified\": true,\n" +
                "                            \"name_en\": \"\",\n" +
                "                            \"icon\": \"http://media.breadtrip.com/images/icons/poi_category_11.png\",\n" +
                "                            \"name\": \"长江索道\",\n" +
                "                            \"popularity\": 809\n" +
                "                        },\n" +
                "                        \"cover_image_height\": 1067,\n" +
                "                        \"trip_id\": 2387276098,\n" +
                "                        \"index_title\": \"\",\n" +
                "                        \"center_point\": {\n" +
                "                            \"lat\": 0,\n" +
                "                            \"lng\": 0\n" +
                "                        },\n" +
                "                        \"view_count\": 49744,\n" +
                "                        \"location_alias\": \"\",\n" +
                "                        \"cover_image_1600\": \"http://photos.breadtrip.com/photo_d_2016_06_24_7df54edb43492917e7d1cb743a4a379b92bc85c2562bb6b1a39d346c4f6fc0e9.jpg?imageView/2/w/1384/h/1384/q/85\",\n" +
                "                        \"cover_image_s\": \"http://photos.breadtrip.com/photo_d_2016_06_24_7df54edb43492917e7d1cb743a4a379b92bc85c2562bb6b1a39d346c4f6fc0e9.jpg?imageView/1/w/280/h/280/q/75\",\n" +
                "                        \"share_url\": \"btrip/spot/2387843168/\",\n" +
                "                        \"timezone\": \"Asia/Shanghai\",\n" +
                "                        \"date_tour\": \"2016-06-24T10:51:14+08:00\",\n" +
                "                        \"is_hiding_location\": false,\n" +
                "                        \"user\": {\n" +
                "                            \"location_name\": \"\",\n" +
                "                            \"name\": \"W猫小北\",\n" +
                "                            \"resident_city_id\": 288652,\n" +
                "                            \"mobile\": \"\",\n" +
                "                            \"gender\": 1,\n" +
                "                            \"avatar_m\": \"http://photos.breadtrip.com/avatar_bc_fd_a6720f5e50d1f22194c51f73329f0e9c48ab7ba8.jpg-avatar.m\",\n" +
                "                            \"cover\": \"http://photos.breadtrip.com/default_user_cover_05.jpg-usercover.display\",\n" +
                "                            \"custom_url\": \"\",\n" +
                "                            \"experience\": {\n" +
                "                                \"value\": 1242,\n" +
                "                                \"level_info\": {\n" +
                "                                    \"name\": \"\",\n" +
                "                                    \"value\": 4\n" +
                "                                }\n" +
                "                            },\n" +
                "                            \"id\": 2387577484,\n" +
                "                            \"birthday\": \"\",\n" +
                "                            \"country_num\": \"86\",\n" +
                "                            \"avatar_s\": \"http://photos.breadtrip.com/avatar_bc_fd_a6720f5e50d1f22194c51f73329f0e9c48ab7ba8.jpg-avatar.s\",\n" +
                "                            \"country_code\": \"CN\",\n" +
                "                            \"email_verified\": false,\n" +
                "                            \"is_hunter\": true,\n" +
                "                            \"cdc2\": false,\n" +
                "                            \"avatar_l\": \"http://photos.breadtrip.com/avatar_bc_fd_a6720f5e50d1f22194c51f73329f0e9c48ab7ba8.jpg-avatar.l\",\n" +
                "                            \"email\": \"\",\n" +
                "                            \"user_desc\": \"我是猫小北，喜欢摄影摄像以及美食旅行，独自旅行大半中国，特长就是长得帅，吃得多还不胖。喜欢用心记录不容易发现的美好，我想记录这一切的美好。\",\n" +
                "                            \"points\": 87\n" +
                "                        },\n" +
                "                        \"spot_id\": 2387843168,\n" +
                "                        \"is_author\": false,\n" +
                "                        \"cover_image_w640\": \"http://photos.breadtrip.com/photo_d_2016_06_24_7df54edb43492917e7d1cb743a4a379b92bc85c2562bb6b1a39d346c4f6fc0e9.jpg?imageView/1/w/640/h/480/q/85\",\n" +
                "                        \"region\": {\n" +
                "                            \"primary\": \"\",\n" +
                "                            \"secondary\": \"\"\n" +
                "                        },\n" +
                "                        \"comments_count\": 7,\n" +
                "                        \"cover_image\": \"http://photos.breadtrip.com/photo_d_2016_06_24_7df54edb43492917e7d1cb743a4a379b92bc85c2562bb6b1a39d346c4f6fc0e9.jpg?imageView/2/w/960/q/85\",\n" +
                "                        \"cover_image_width\": 1600,\n" +
                "                        \"recommendations_count\": 40\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"desc\": \"\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 10,\n" +
                "                \"data\": [\n" +
                "                    {\n" +
                "                        \"text\": \"很有幸能有王老这样的人让我们能睹旧物思旧人旧事，陈列室虽不大，却也充斥了老北京饮食医用居的方方面面，可以亲手触摸历史，了解自己现在工作城市的过去现在让人倍感亲切！\",\n" +
                "                        \"is_liked\": false,\n" +
                "                        \"index_cover\": \"http://photos.breadtrip.com/photo_d_2016_06_09_23_56_51_174_123986672_-1220261189.jpg?imageView/2/w/960/q/85\",\n" +
                "                        \"poi\": \"\",\n" +
                "                        \"cover_image_height\": 724,\n" +
                "                        \"trip_id\": 2387247307,\n" +
                "                        \"index_title\": \"\",\n" +
                "                        \"center_point\": {},\n" +
                "                        \"view_count\": 39166,\n" +
                "                        \"location_alias\": \"\",\n" +
                "                        \"cover_image_1600\": \"http://photos.breadtrip.com/photo_d_2016_06_09_23_56_50_703_123986672_834948024.jpg?imageView/2/w/1384/h/1384/q/85\",\n" +
                "                        \"cover_image_s\": \"http://photos.breadtrip.com/photo_d_2016_06_09_23_56_50_703_123986672_834948024.jpg?imageView/1/w/280/h/280/q/75\",\n" +
                "                        \"share_url\": \"btrip/spot/2387849028/\",\n" +
                "                        \"timezone\": \"Asia/Shanghai\",\n" +
                "                        \"date_tour\": \"2016-06-09T23:42:30+08:00\",\n" +
                "                        \"is_hiding_location\": false,\n" +
                "                        \"user\": {\n" +
                "                            \"location_name\": \"\",\n" +
                "                            \"name\": \"Lucy鱼er\",\n" +
                "                            \"resident_city_id\": \"\",\n" +
                "                            \"mobile\": \"\",\n" +
                "                            \"gender\": 2,\n" +
                "                            \"avatar_m\": \"http://photos.breadtrip.com/avatar_d0_f2_9450f59011820dcdfe7f0317d76f7fb0654cf52c.jpg-avatar.m\",\n" +
                "                            \"cover\": \"http://media.breadtrip.com/user_covers/default/cover_3.jpg\",\n" +
                "                            \"custom_url\": \"\",\n" +
                "                            \"experience\": {\n" +
                "                                \"value\": 132,\n" +
                "                                \"level_info\": {\n" +
                "                                    \"name\": \"\",\n" +
                "                                    \"value\": 2\n" +
                "                                }\n" +
                "                            },\n" +
                "                            \"id\": 2386840189,\n" +
                "                            \"birthday\": \"\",\n" +
                "                            \"country_num\": null,\n" +
                "                            \"avatar_s\": \"http://photos.breadtrip.com/avatar_d0_f2_9450f59011820dcdfe7f0317d76f7fb0654cf52c.jpg-avatar.s\",\n" +
                "                            \"country_code\": null,\n" +
                "                            \"email_verified\": false,\n" +
                "                            \"is_hunter\": false,\n" +
                "                            \"cdc2\": false,\n" +
                "                            \"avatar_l\": \"http://photos.breadtrip.com/avatar_d0_f2_9450f59011820dcdfe7f0317d76f7fb0654cf52c.jpg-avatar.l\",\n" +
                "                            \"email\": \"\",\n" +
                "                            \"user_desc\": \"\",\n" +
                "                            \"points\": 52\n" +
                "                        },\n" +
                "                        \"spot_id\": 2387849028,\n" +
                "                        \"is_author\": false,\n" +
                "                        \"cover_image_w640\": \"http://photos.breadtrip.com/photo_d_2016_06_09_23_56_50_703_123986672_834948024.jpg?imageView/1/w/640/h/480/q/85\",\n" +
                "                        \"region\": {\n" +
                "                            \"primary\": \"\",\n" +
                "                            \"secondary\": \"\"\n" +
                "                        },\n" +
                "                        \"comments_count\": 2,\n" +
                "                        \"cover_image\": \"http://photos.breadtrip.com/photo_d_2016_06_09_23_56_50_703_123986672_834948024.jpg?imageView/2/w/960/q/85\",\n" +
                "                        \"cover_image_width\": 965,\n" +
                "                        \"recommendations_count\": 13\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"desc\": \"\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 10,\n" +
                "                \"data\": [\n" +
                "                    {\n" +
                "                        \"text\": \"献给爱我们的女神\",\n" +
                "                        \"is_liked\": false,\n" +
                "                        \"index_cover\": \"http://photos.breadtrip.com/photo_d_2016_06_19_01_21_20_989_123986672_17737936936133063098.jpg?imageView/2/w/960/q/85\",\n" +
                "                        \"poi\": \"\",\n" +
                "                        \"cover_image_height\": 816,\n" +
                "                        \"trip_id\": 2387282916,\n" +
                "                        \"index_title\": \"\",\n" +
                "                        \"center_point\": {},\n" +
                "                        \"view_count\": 36207,\n" +
                "                        \"location_alias\": \"\",\n" +
                "                        \"cover_image_1600\": \"http://photos.breadtrip.com/photo_d_2016_06_19_01_21_20_926_123986672_17737936923172662193.jpg?imageView/2/w/1384/h/1384/q/85\",\n" +
                "                        \"cover_image_s\": \"http://photos.breadtrip.com/photo_d_2016_06_19_01_21_20_926_123986672_17737936923172662193.jpg?imageView/1/w/280/h/280/q/75\",\n" +
                "                        \"share_url\": \"btrip/spot/2387842143/\",\n" +
                "                        \"timezone\": \"Asia/Shanghai\",\n" +
                "                        \"date_tour\": \"2016-06-19T01:19:07+08:00\",\n" +
                "                        \"is_hiding_location\": false,\n" +
                "                        \"user\": {\n" +
                "                            \"location_name\": \"\",\n" +
                "                            \"name\": \"丑到没墙角\",\n" +
                "                            \"resident_city_id\": \"\",\n" +
                "                            \"mobile\": \"\",\n" +
                "                            \"gender\": 2,\n" +
                "                            \"avatar_m\": \"http://photos.breadtrip.com/avatar_41_b8_aedfd71640e3ec09d0c30edc47df04dc56dbf38a.jpg-avatar.m\",\n" +
                "                            \"cover\": \"http://photos.breadtrip.com/default_user_cover_10.jpg-usercover.display\",\n" +
                "                            \"custom_url\": \"\",\n" +
                "                            \"experience\": {\n" +
                "                                \"value\": 59,\n" +
                "                                \"level_info\": {\n" +
                "                                    \"name\": \"\",\n" +
                "                                    \"value\": 1\n" +
                "                                }\n" +
                "                            },\n" +
                "                            \"id\": 2384288641,\n" +
                "                            \"birthday\": \"\",\n" +
                "                            \"country_num\": null,\n" +
                "                            \"avatar_s\": \"http://photos.breadtrip.com/avatar_41_b8_aedfd71640e3ec09d0c30edc47df04dc56dbf38a.jpg-avatar.s\",\n" +
                "                            \"country_code\": null,\n" +
                "                            \"email_verified\": false,\n" +
                "                            \"is_hunter\": false,\n" +
                "                            \"cdc2\": false,\n" +
                "                            \"avatar_l\": \"http://photos.breadtrip.com/avatar_41_b8_aedfd71640e3ec09d0c30edc47df04dc56dbf38a.jpg-avatar.l\",\n" +
                "                            \"email\": \"\",\n" +
                "                            \"user_desc\": \"\",\n" +
                "                            \"points\": 2\n" +
                "                        },\n" +
                "                        \"spot_id\": 2387842143,\n" +
                "                        \"is_author\": false,\n" +
                "                        \"cover_image_w640\": \"http://photos.breadtrip.com/photo_d_2016_06_19_01_21_20_926_123986672_17737936923172662193.jpg?imageView/1/w/640/h/480/q/85\",\n" +
                "                        \"region\": {\n" +
                "                            \"primary\": \"\",\n" +
                "                            \"secondary\": \"\"\n" +
                "                        },\n" +
                "                        \"comments_count\": 2,\n" +
                "                        \"cover_image\": \"http://photos.breadtrip.com/photo_d_2016_06_19_01_21_20_926_123986672_17737936923172662193.jpg?imageView/2/w/960/q/85\",\n" +
                "                        \"cover_image_width\": 1088,\n" +
                "                        \"recommendations_count\": 21\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"desc\": \"\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 9,\n" +
                "                \"data\": [\n" +
                "                    {\n" +
                "                        \"title\": \"精彩原创和专题\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"desc\": \"\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 4,\n" +
                "                \"data\": [\n" +
                "                    {\n" +
                "                        \"cover_image_default\": \"http://photos.breadtrip.com/photo_2017_07_18_0996aaecaadfdc9ed3534ed9b0c4928c.jpg?imageView/2/w/960/q/85\",\n" +
                "                        \"waypoints\": 153,\n" +
                "                        \"wifi_sync\": false,\n" +
                "                        \"last_day\": \"2017-06-14\",\n" +
                "                        \"id\": 2387425140,\n" +
                "                        \"view_count\": 25191,\n" +
                "                        \"privacy\": 0,\n" +
                "                        \"day_count\": 7,\n" +
                "                        \"index_title\": \"荷兰--黄金时代的回响";
        JsonValidator jsonValidator = new JsonValidator();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            jsonValidator.validate(jsonStr);
        }
        long end = System.currentTimeMillis();
        System.out.println("耗时：" + (end - start));
    }
}
