// noinspection JSUnusedGlobalSymbols,ES6ConvertVarToLetConst,JSUnresolvedReference
// noinspection ES6ConvertVarToLetConst
var request = {
    variables: {
        dataHolder: {},
        get: function (key) {
            return this.dataHolder[key] !== undefined ? this.dataHolder[key] : null;
        },
        isEmpty: function () {
            return Object.keys(this.dataHolder).length === 0;
        },
        clear: function (key) {
            delete this.dataHolder[key];
        },
        clearAll: function () {
            this.dataHolder = {};
        },
        set: function (key, val) {
            var desc
            if (val !== null) {
                val = val + '';
                if (val.length > 300) {
                    desc = val.substring(0, 300) + `...(${CONTENT_TRUNCATED})`
                } else {
                    desc = val;
                }
            } else {
                desc = "null";
            }

            this.dataHolder[key] = val;
            globalLog.log(key + ` ${REQUEST_SET} ` + desc);
        },
    }
};

function hasRequestVariableKey(key) {
    return Object.keys(request.variables.dataHolder).indexOf(key) !== -1;
}

function getRequestVariable(key) {
    return request.variables.get(key);
}

var Window = {
    btoa: function btoa(bytes) {
        return javaBridge.btoa(bytes);
    },
    atob: function atob(str) {
        return javaBridge.atob(str);
    }
}

function base64ToFile(base64, path) {
    javaBridge.base64ToFile(base64, path);
}

function jsonPath(obj, expression) {
    return javaBridge.jsonPath(obj, expression);
}

function xpath(obj, expression) {
    return javaBridge.xpath(obj, expression);
}