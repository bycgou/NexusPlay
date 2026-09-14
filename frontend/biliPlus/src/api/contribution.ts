// 投稿页面请求

import request from "@/utils/request.ts";

// 提交视频审核
export const submitContribution = (data) => {
    return request({
        url: '/pp/people/contribution',
        method: 'post',
        data
    });
};

// 上传图片接口
