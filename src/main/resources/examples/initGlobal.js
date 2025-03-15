// noinspection JSUnusedGlobalSymbols,ES6ConvertVarToLetConst,JSUnresolvedReference
// noinspection ES6ConvertVarToLetConst
var client = {
    log: function (msg) {
        return globalLog.log(msg);
    },
    global: {
        dataHolder: {},
        isEmpty: function () {
            return Object.keys(this.dataHolder).length === 0;
        },
        get: function (key) {
            return this.dataHolder[key] !== undefined ? this.dataHolder[key] : null;
        },
        clear: function (key) {
            return delete this.dataHolder[key];
        },
        clearAll: function () {
            this.dataHolder = {};
            return true;
        },
        set: function (key, val) {
            var desc
            if (val !== null) {
                val = val + '';
                if (val.length > 300) {
                    desc = val.substring(0, 300) + "...(已截断显示)"
                } else {
                    desc = val;
                }
            } else {
                desc = "null";
            }

            this.dataHolder[key] = val;
            globalLog.log(key + ' 已设置为(global): ' + desc);
        },
    },
    test: function (successMsg, assertCallback) {
        try {
            assertCallback();
            globalLog.log(successMsg);
        } catch (e) {
            globalLog.log(e);
        }
    },
    assert: function (success, failMsg) {
        if (!success) {
            throw new Error(failMsg);
        }
    }
};

var console = {
    log: function (msg) {
        return globalLog.log(msg);
    }
}

function hasGlobalVariableKey(key) {
    return Object.keys(client.global.dataHolder).indexOf(key) !== -1;
}

function getGlobalVariable(key) {
    return client.global.get(key);
}