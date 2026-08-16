package com.shortlink.admin.controller;

import com.shortlink.admin.pojo.dto.ShortLinkCreateDTO;
import com.shortlink.admin.pojo.dto.ShortLinkPageDTO;
import com.shortlink.admin.pojo.dto.ShortLinkUpdateDTO;
import com.shortlink.admin.pojo.vo.ShortLinkCreateVO;
import com.shortlink.admin.pojo.vo.ShortLinkVO;
import com.shortlink.admin.service.ShortLinkService;
import com.shortlink.common.result.PageResult;
import com.shortlink.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "短链管理")
@RestController
@RequestMapping("/api/admin/link")
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    @Operation(summary = "创建短链")
    @PostMapping
    public Result<ShortLinkCreateVO> create(@Valid @RequestBody ShortLinkCreateDTO createDTO) {
        return Result.success(shortLinkService.createShortLink(createDTO));
    }

    @Operation(summary = "更新短链")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody ShortLinkUpdateDTO updateDTO) {
        shortLinkService.updateShortLink(updateDTO);
        return Result.success();
    }

    @Operation(summary = "修改短链状态（0-启用 1-禁用）")
    @PutMapping("/{shortCode}/status/{status}")
    public Result<Void> updateStatus(@PathVariable("shortCode") String shortCode,
                                     @PathVariable("status") Integer status) {
        shortLinkService.updateStatus(shortCode, status);
        return Result.success();
    }

    @Operation(summary = "删除短链")
    @DeleteMapping("/{shortCode}")
    public Result<Void> delete(@PathVariable("shortCode") String shortCode) {
        shortLinkService.deleteShortLink(shortCode);
        return Result.success();
    }

    @Operation(summary = "短码查详情")
    @GetMapping("/{shortCode}")
    public Result<ShortLinkVO> detail(@PathVariable("shortCode") String shortCode) {
        return Result.success(shortLinkService.getShortLink(shortCode));
    }

    @Operation(summary = "分页条件查询")
    @GetMapping("/page")
    public Result<PageResult<ShortLinkVO>> page(ShortLinkPageDTO pageDTO) {
        return Result.success(shortLinkService.pageShortLink(pageDTO));
    }
}
