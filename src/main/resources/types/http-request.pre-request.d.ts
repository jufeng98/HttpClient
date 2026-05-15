// noinspection JSUnusedGlobalSymbols

interface HttpClientRequest {
    /**
     * 当前请求体信息
     */
    body: RequestBody;

    /**
     * 当前请求的url信息
     */
    url: RequestUrl;
}


interface RequestVariables {
    /**
     * 保存名为“varName”的变量，并将其值设置为“varValue”。
     */
    set(varName: string, varValue: any): void;

    /**
     * 按名称检索请求变量值。如果没有这样的变量，则返回null
     * @param varName 请求变量名称
     */
    get(varName: string): any;

    /**
     * 检查是否未定义任何变量
     */
    isEmpty(): boolean;

    /**
     * 清除变量 'varName'.
     * @param varName {string}
     */
    clear(varName: string): void;

    /**
     * 清除所有变量.
     */
    clearAll(): void;
}

/**
 * 访问当前请求体的接口。在前置js处理器中body可能尚未构建
 */
interface RequestBody {
    /**
     * 获取原始body值，不包含任何替换变量。因此，所有{{var}}部分将保持不变。
     */
    getRaw(): string;

    /**
     * 尝试替换body中的已知变量并返回结果。所有已知的{{var}}将被它们的值替换。未知｛｛var｝｝将保持不变。
     */
    tryGetSubstituted(): string;
}

/**
 * 访问当前url的接口。在前置js处理器中url可能尚未构建
 */
interface RequestUrl {
    /**
     * 获取原始URL值，不包含任何替换变量。因此，所有{{var}}部分将保持不变。
     */
    getRaw(): string;

    /**
     * 尝试替换URL中的已知变量并返回结果。所有已知的{{var}}将被它们的值替换。未知｛｛var｝｝将保持不变。
     */
    tryGetSubstituted(): string;
}

interface RequestHeader {
    /**
     * 获取原始标头值，不包含任何替换变量。因此，所有{{var}}部分将保持不变。
     */
    getRawValue(): string;

    /**
     * 尝试替换头值中的已知变量并返回结果。所有已知的{{var}}将被它们的值替换。未知｛｛var｝｝将保持不变。
     */
    tryGetSubstitutedValue(): string;
}