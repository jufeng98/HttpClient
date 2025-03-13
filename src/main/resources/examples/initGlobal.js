// noinspection JSUnusedGlobalSymbols,ES6ConvertVarToLetConst
// noinspection ES6ConvertVarToLetConst
var client = {
    fullMsg: '',
    log: function (msg) {
        this.fullMsg = this.fullMsg + msg + '\r\n';
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
            client.log(key + ' 已设置为(global): ' + desc);
        },
    },
    test: function (successMsg, assertCallback) {
        const success = assertCallback();
        if (success === true || success === undefined) {
            this.log(successMsg);
        }
    },
    assert: function (success, failMsg) {
        if (!success) {
            this.log("断言失败: " + failMsg);
        }
        return success;
    }
};

var console = {
    log: function (msg) {
        return client.log(msg);
    }
}

function getLog() {
    var tmp = client.fullMsg;
    client.fullMsg = '';
    return tmp;
}

function hasGlobalVariableKey(key) {
    return Object.keys(client.global.dataHolder).indexOf(key) !== -1;
}

function getGlobalVariable(key) {
    return client.global.get(key);
}