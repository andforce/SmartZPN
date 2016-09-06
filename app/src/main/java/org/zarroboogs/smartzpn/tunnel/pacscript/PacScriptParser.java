package org.zarroboogs.smartzpn.tunnel.pacscript;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by wangdiyuan on 16-9-6.
 */
public class PacScriptParser {

    private String mScript;
    public PacScriptParser(String script){
        mScript = shExpMatch + "\n" + isInNet + "\n" + script;
    }

    private static final String shExpMatch = "var shExpMatch = function (){\n" +
            "    var _map = { '.': '\\\\.', '*': '.*?', '?': '.' };\n" +
            "    var _rep = function (m){ return _map[m] };\n" +
            "    return function (text, exp){\n" +
            "        return new RegExp(exp.replace(/\\.|\\*|\\?/g, _rep)).test(text);\n" +
            "    };\n" +
            "}();";

    private static final String isInNet = "var isInNet = function (){\n" +
            "    function convert_addr(ipchars) {\n" +
            "        var bytes = ipchars.split('.');\n" +
            "        return ((bytes[0] & 0xff) << 24) |\n" +
            "            ((bytes[1] & 0xff) << 16) |\n" +
            "            ((bytes[2] & 0xff) <<  8) |\n" +
            "            (bytes[3] & 0xff);\n" +
            "    }\n" +
            "    return function (ipaddr, pattern, maskstr) {\n" +
            "        var match = /^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$/.exec(ipaddr);\n" +
            "        if (match[1] > 255 || match[2] > 255 ||\n" +
            "            match[3] > 255 || match[4] > 255) {\n" +
            "            return false;    // not an IP address\n" +
            "        }\n" +
            "        var host = convert_addr(ipaddr);\n" +
            "        var pat  = convert_addr(pattern);\n" +
            "        var mask = convert_addr(maskstr);\n" +
            "        return ((host & mask) == (pat & mask));\n" +
            "    };\n" +
            "}();";

    private String runScript(String js, String functionName, Object[] functionParams) {
        Context rhino = Context.enter();
        rhino.setOptimizationLevel(-1);
        try {
            Scriptable scope = rhino.initStandardObjects();
            rhino.evaluateString(scope, js, "JavaScript", js.split("\n").length, null);

            Function function = (Function) scope.get(functionName, scope);

            Object result = function.call(rhino, scope, scope, functionParams);
            if (result instanceof String) {
                return (String) result;
            } else if (result instanceof NativeJavaObject) {
                return (String) ((NativeJavaObject) result).getDefaultValue(String.class);
            } else if (result instanceof NativeObject) {
                return (String) ((NativeObject) result).getDefaultValue(String.class);
            }
            return result.toString();
        } finally {
            Context.exit();
        }
    }

    public String findProxyForURL(String url, String host){
        return runScript(mScript, "FindProxyForURL", new String[]{url, host});
    }
}
