package com.skuniv.bigdata.batch.items;

import com.google.gson.Gson;
import com.skuniv.bigdata.domain.dto.BargainOpenApiDto;
import com.skuniv.bigdata.domain.dto.CharterWithRentOpenApiDto;
import com.skuniv.bigdata.domain.dto.YamlDto;
import com.skuniv.bigdata.domain.dto.open_api.BargainItemDto;
import com.skuniv.bigdata.domain.dto.open_api.BuildingDealDto;
import com.skuniv.bigdata.domain.dto.open_api.CharterWithRentItemDto;
import com.skuniv.bigdata.util.OpenApiConstants;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@StepScope
@Component
@RequiredArgsConstructor
public class OpenApiWriter implements ItemWriter<BuildingDealDto>, StepExecutionListener {
    private static final Gson gson = new Gson();
    private final YamlDto yamlDto;

    private String fileName;
    private BufferedWriter bufferedWriter;

    private void write(BufferedWriter bw, String content) throws IOException {
        bw.write(content);
        bw.newLine();
    }

    private void divisionItem(BuildingDealDto item) throws IOException {
        if (StringUtils.equals(item.getDealType(), OpenApiConstants.BARGAIN_NUM)) {
            BargainOpenApiDto bargainOpenApiDto = (BargainOpenApiDto) item;
            for (BargainItemDto bargainItemDto : bargainOpenApiDto.getBody().getItem()) {
                write(bufferedWriter, gson.toJson(bargainItemDto));
            }
        } else {
            CharterWithRentOpenApiDto charterWithRentOpenApiDto = (CharterWithRentOpenApiDto) item;
            for (CharterWithRentItemDto charterWithRentItemDto : charterWithRentOpenApiDto.getBody().getItem()) {
                write(bufferedWriter, gson.toJson(charterWithRentItemDto));
            }
        }
    }

    @Override
    public void write(List<? extends BuildingDealDto> items) throws Exception {
        for (BuildingDealDto item : items) {
            // 파일 쓰기
            divisionItem(item);
        }
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        ExecutionContext ctx = stepExecution.getExecutionContext();
        fileName = (String) ctx.get(OpenApiConstants.API_KIND);
        String fileFullPath = yamlDto.getFilePath() + OpenApiConstants.FILE_DELEMETER + fileName;
        File f = new File(fileFullPath);
        if (f.exists() && !f.isDirectory()) {
            StringBuilder sb = new StringBuilder();
            sb.append("mv ").append(fileFullPath).append(" ").append(fileFullPath).append(OpenApiConstants.OLD);
            try {
                Runtime.getRuntime().exec(sb.toString());
                bufferedWriter = new BufferedWriter(new FileWriter(fileFullPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
}