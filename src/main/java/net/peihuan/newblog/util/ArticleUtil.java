package net.peihuan.newblog.util;

import lombok.experimental.UtilityClass;
import net.peihuan.newblog.bean.entity.Article;
import net.peihuan.newblog.bean.vo.ArticleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@UtilityClass
public class ArticleUtil {

    private final String SEPARATED_CONSTANT = ",";


    public String trimPictureTitle(String title) {
        return title.replace(" ", "");
    }

    public ArticleVO convert2VO(Article article) {
        return convert2VO(article, false);
    }

    public ArticleVO convert2VO(Article article, boolean content) {
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);
        vo.setTags(str2List(article.getTags()));
        vo.setCategories(str2List(article.getCategories()));
        if (!content) {
            vo.setContent(null);
        }
        return vo;
    }


    public List<ArticleVO> convert2VO(List<Article> articles) {
        return articles.stream().map(ArticleUtil::convert2VO).collect(Collectors.toList());
    }

    public String list2Str(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        StringJoiner joiner = new StringJoiner(SEPARATED_CONSTANT);
        values.forEach(joiner::add);
        return joiner.toString();
    }

    public List<String> str2List(String value) {
        if (!StringUtils.hasText(value)) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split(SEPARATED_CONSTANT));
    }
}
