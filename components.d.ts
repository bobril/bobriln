import * as b from './bobril';
export interface IViewData {
    style?: b.IBobrilStyles;
    children?: b.IBobrilChildren;
}
export declare function View(data?: IViewData, children?: b.IBobrilChildren): {
    tag: string;
    children: b.IBobrilChildren;
};
export interface ITextData {
    style?: b.IBobrilStyles;
    children?: b.IBobrilChildren;
}
export declare function Text(data?: IViewData, children?: b.IBobrilChildren): {
    tag: string;
    children: b.IBobrilChildren;
};
export interface IImageData {
    /** pass result of b.asset("pathToImage") */
    source: string | (string | number)[];
    style?: b.IBobrilStyles;
    children?: b.IBobrilChildren;
}
export declare function Image(data: IImageData, children?: b.IBobrilChildren): {
    tag: string;
    attrs: {
        source: string | (string | number)[];
    };
    children: b.IBobrilChildren;
};
export interface ISwitchData extends b.IValueData<boolean> {
    style?: b.IBobrilStyles;
}
export declare const Switch: b.IComponentFactory<ISwitchData>;
export interface ITextInputData extends b.IValueData<string> {
    children?: b.IBobrilChildren;
    style?: b.IBobrilStyles;
    selectionStart?: number;
    selectionEnd?: number;
    onDetailChange?: (start: number, before: number, text: string) => void;
    onSelectionChange?: (start: number, end: number) => void;
}
export declare const TextInput: b.IComponentFactory<ITextInputData>;
export interface IScrollViewData {
    horizontal?: boolean;
    style?: b.IBobrilStyles;
    children?: b.IBobrilChildren;
}
export declare function ScrollView(data: IScrollViewData, children?: b.IBobrilChildren): b.IBobrilNodeWithTag | b.IBobrilNodeWithChildren;
