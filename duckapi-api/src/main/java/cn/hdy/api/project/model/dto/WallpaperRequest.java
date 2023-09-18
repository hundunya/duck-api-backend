package cn.hdy.api.project.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 混沌鸭
 *
 * 壁纸请求
 **/
@Data
public class WallpaperRequest implements Serializable {
    /**
     * 输出壁纸端[mobile|pc|zsy]默认为pc
     */
    private String method;

    /**
     * 	选择输出分类[meizi|dongman|fengjing|suiji]，为空随机输出
     */
    private String lx;

    /**
     * 输出壁纸格式[json|images]默认为images
     */
    private String format;
}
