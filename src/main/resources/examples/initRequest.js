// noinspection JSUnusedGlobalSymbols,ES6ConvertVarToLetConst,JSUnresolvedReference
// noinspection ES6ConvertVarToLetConst
var request = {
    variables: {
        dataHolder: {},
        get: function (key) {
            return this.dataHolder[key] !== undefined ? this.dataHolder[key] : null;
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
            globalLog.log(key + ' 已设置为(request): ' + desc);
        },
    }
};

function hasRequestVariableKey(key) {
    return Object.keys(request.variables.dataHolder).indexOf(key) !== -1;
}

function getRequestVariable(key) {
    return request.variables.get(key);
}