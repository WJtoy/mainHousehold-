/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.md.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.kkl.kklplus.entity.md.MDDisableWord;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.SpringContextHolder;
import com.wolfking.jeesite.ms.providermd.service.MSDisableWordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;

/**
 * 禁用词工具类
 *
 * @author Ryan
 * @version 2021-04-06
 */
@Component
public class DisableWordUtils {

    private static MSDisableWordService disableWordService = SpringContextHolder.getBean(MSDisableWordService.class);

    private static Cache<String, Object> caffeineCache = SpringContextHolder.getBean(Cache.class);

    private static final String CASH_KEY = "disableWord";

    /**
     * 所有禁用词
     *
     * @return
     */
    public static String getAllWordsString() {
        //get from cach
        String words = (String)caffeineCache.getIfPresent(CASH_KEY);
        if(StringUtils.isBlank(words)){
            words = getAllWordsFromMSService();
            if(StringUtils.isNotBlank(words)){
                caffeineCache.put(CASH_KEY,words);
            }
        }
        if(StringUtils.isBlank(words)){
            return StringUtils.EMPTY;
        }

        return words;
    }

    /**
     * 所有禁用词字符
     *
     * @return
     */
    private static String getAllWordsFromMSService() {
        Page<MDDisableWord> page = new Page<>(1,1000);
        MDDisableWord mdDisableWord = new MDDisableWord();
        Page<MDDisableWord> list = disableWordService.findList(page, mdDisableWord);
        if(CollectionUtils.isEmpty(list.getList())){
            return StringUtils.EMPTY;
        }
        return list.getList().stream().map(t-> "'"+t.getWord()+"'").collect(Collectors.joining(","));
    }

}
