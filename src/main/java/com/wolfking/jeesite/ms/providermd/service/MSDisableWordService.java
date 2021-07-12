package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDDisableWord;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.feign.MSDisableWordFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MSDisableWordService {

    @Autowired
    private MSDisableWordFeign msDisableWordFeign;

    public Page<MDDisableWord> findList(Page<MDDisableWord> page,MDDisableWord mdDisableWord){
        Page<MDDisableWord> tPage = new Page<>();
        tPage.setPageSize(page.getPageSize());
        tPage.setPageNo(page.getPageNo());
        mdDisableWord.setPage(new MSPage<>(tPage.getPageNo(), tPage.getPageSize()));
        MSResponse<MSPage<MDDisableWord>> msResponse = msDisableWordFeign.findList(mdDisableWord);
        if (MSResponse.isSuccess(msResponse)) {
            MSPage<MDDisableWord>  msPage = msResponse.getData();
            page.setList(msPage.getList());
            page.setCount(msPage.getRowCount());
        } else {
            page.setCount(0);
            page.setList(new ArrayList<>());
        }
        return page;
    }

    public void save(MDDisableWord mdDisableWord){
        User user = UserUtils.getUser();
        List<String> words;
        MDDisableWord disableWord;
        List<MDDisableWord> mdDisableWordList = Lists.newArrayList();
        if(mdDisableWord.getWord() != null && !mdDisableWord.getWord().equals("")){
            String replace = mdDisableWord.getWord().replace("，", ",").replace("、", ",");
            words = Arrays.stream(replace.split(",")).collect(Collectors.toList());
            for(String word:words){
                disableWord = new MDDisableWord();
                disableWord.setWord(word);
                disableWord.setCreateById(user.getId());
                disableWord.setCreateDate(new Date());
                disableWord.setUpdateById(user.getId());
                disableWord.setUpdateDate(new Date());
                mdDisableWordList.add(disableWord);
            }
            msDisableWordFeign.batchInsert(mdDisableWordList);

        }
    }

    public void   delete(Long id){
        User user = UserUtils.getUser();
        MDDisableWord mdDisableWord = new MDDisableWord();
        mdDisableWord.setId(id);
        mdDisableWord.setUpdateById(user.getId());
        mdDisableWord.setUpdateDate(new Date());
        MSResponse<Integer> msResponse = msDisableWordFeign.delete(mdDisableWord);
        if(!MSResponse.isSuccessCode(msResponse)){
            throw new RuntimeException(msResponse.getMsg());
        }
    }
}
