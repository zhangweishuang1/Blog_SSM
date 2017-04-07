package tech.acodesigner.web;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tech.acodesigner.dto.ArticleDto;
import tech.acodesigner.dto.CategoryDto;
import tech.acodesigner.dto.OperationResult;
import tech.acodesigner.entity.Article;
import tech.acodesigner.entity.Category;
import tech.acodesigner.entity.Link;
import tech.acodesigner.service.AboutService;
import tech.acodesigner.service.ArticleService;
import tech.acodesigner.service.CategoryService;
import tech.acodesigner.service.LinkService;
import tech.acodesigner.util.ImagesUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Created by 77239 on 2017/4/4/0004.
 */
@SuppressWarnings("Since15")
@Controller
@RequestMapping("/manage")
public class ManageController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private LinkService linkService;

    @Autowired
    private AboutService aboutService;

    @RequestMapping(method = RequestMethod.GET)
    public String showManageView(Model model) {
        model.addAttribute("mainPage", "manageView.jsp");
        return "manage/manage";
    }

    //对文章的管理
    @RequestMapping(value = "/article", method = RequestMethod.GET)
    public String showArticleList(Model model) {
        List<ArticleDto> articles = articleService.getArticles();
        model.addAttribute("articles", articles);
        model.addAttribute("mainPage", "article.jsp");
        return "manage/manage";
    }

    @RequestMapping(value = {"/article/write", "/article/modify/{articleId}"}, method = RequestMethod.GET)
    public String editArticle(@PathVariable("articleId") Optional<Integer> articleId, HttpServletRequest request, Model model, RedirectAttributes attributes) {
        if (articleId.isPresent()) {
            OperationResult<ArticleDto> result = articleService.getArticleById(articleId.get());
            if (result.isSuccess()) {
                model.addAttribute("article", result.getData());
            } else {
                attributes.addFlashAttribute("info", result.getInfo());
                return "redirect:/manage/article";
            }
        }

        List<CategoryDto> categories = categoryService.getCategories();
        model.addAttribute("categories", categories);

        String[] images = ImagesUtil.getImages(request.getServletContext().getRealPath("images/article"));
        model.addAttribute("images", images);

        model.addAttribute("mainPage", "articleEditor.jsp");
        return "manage/manage";
    }

    @RequestMapping(value = {"/article/save", "/article/save/{articleId}"}, method = RequestMethod.POST)
    public String saveArticle(@PathVariable("articleId") Optional<Integer> articleId, HttpServletRequest request, RedirectAttributes attributes) {
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String categoryId = request.getParameter("categoryId");
        String image = request.getParameter("image");
        Article article = new Article(Integer.parseInt(request.getParameter("categoryId")), request.getParameter("title"),
                request.getParameter("content"), request.getParameter("image"));
        OperationResult result = null;
        if (articleId.isPresent()) {
            article.setClicks(articleService.getArticleById(articleId.get()).getData().getClicks());
            article.setArticleId(articleId.get());
            result = articleService.updateArticle(article);
        } else {
            article.setPubDate(new Date(System.currentTimeMillis()));
            result = articleService.saveArticle(article);
        }
        attributes.addFlashAttribute("info", result.getInfo());
        return "redirect:/manage/article";
    }

    @RequestMapping(value = "/article/delete/{articleId}", method = RequestMethod.GET)
    public String deleteArticle(@PathVariable("articleId") Integer articleId, RedirectAttributes attributes) {
        OperationResult result = articleService.deleteArticle(articleId);
        attributes.addFlashAttribute("info", result.getInfo());
        return "redirect:/manage/article";
    }

    //对类别的管理
    @RequestMapping(value = "/category", method = RequestMethod.GET)
    public String showCategoryList(Model model) {
        List<CategoryDto> categories = categoryService.getCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("mainPage", "category.jsp");
        return "manage/manage";
    }

    @RequestMapping(value = {"/category/add", "/category/modify/{categoryId}"}, method = RequestMethod.GET)
    public String editCategory(@PathVariable("categoryId") Optional<Integer> categoryId, HttpServletRequest request, Model model, RedirectAttributes attributes) {
        if (categoryId.isPresent()) {
            OperationResult<Category> result = categoryService.getCategory(categoryId.get());
            if (result.isSuccess()) {
                model.addAttribute("category", result.getData());
            } else {
                attributes.addFlashAttribute("info", result.getInfo());
                return "redirect:/manage/category";
            }
        }
        model.addAttribute("mainPage", "categoryEditor.jsp");
        return "manage/manage";
    }

    @RequestMapping(value = {"/category/save", "/category/save/{categoryId}"}, method = RequestMethod.POST)
    public String saveCategory(@PathVariable("categoryId") Optional<Integer> categoryId, HttpServletRequest request, RedirectAttributes attributes) {
        String categoryName = request.getParameter("categoryName");
        OperationResult result = null;
        if (categoryId.isPresent()) {
            result = categoryService.updateCategory(new Category(categoryId.get(), categoryName));
        } else {
            result = categoryService.saveCategory(categoryName);
        }
        attributes.addFlashAttribute("info", result.getInfo());
        return "redirect:/manage/category";
    }

    @RequestMapping(value = "/category/delete/{categoryId}", method = RequestMethod.GET)
    public String deleteCategory(@PathVariable("categoryId") Integer categoryId, RedirectAttributes attributes) {
        OperationResult result = categoryService.deleteCategory(categoryId);
        attributes.addFlashAttribute("info", result.getInfo());
        return "redirect:/manage/category";
    }

    //对消息的管理
    //TODO
    //对链接的管理
    @RequestMapping(value = "/link", method = RequestMethod.GET)
    public String showLinkList(Model model) {
        List<Link> links = linkService.getLinks();
        model.addAttribute("links", links);
        model.addAttribute("mainPage", "link.jsp");
        return "manage/manage";
    }

    @RequestMapping(value = {"/link/add", "/link/modify/{linkId}"}, method = RequestMethod.GET)
    public String editLink(@PathVariable("linkId") Optional<Integer> linkId, HttpServletRequest request, Model model, RedirectAttributes attributes) {
        if (linkId.isPresent()) {
            OperationResult<Link> result = linkService.getLinkById(linkId.get());
            if (result.isSuccess()) {
                model.addAttribute("link", result.getData());
            } else {
                attributes.addFlashAttribute("info", result.getInfo());
                return "redirect:/manage/link";
            }
        }
        model.addAttribute("mainPage", "linkEditor.jsp");
        return "manage/manage";
    }

    @RequestMapping(value = {"/link/save", "/link/save/{linkId}"}, method = RequestMethod.POST)
    public String saveLink(@PathVariable("linkId") Optional<Integer> linkId, HttpServletRequest request, RedirectAttributes attributes) {
        String linkName = request.getParameter("linkName");
        String url = request.getParameter("url");
        OperationResult result = null;
        if (linkId.isPresent()) {
            result = linkService.updateLink(new Link(linkId.get(), linkName, url));
        } else {
            result = linkService.saveLink(new Link(linkName, url));
        }
        attributes.addFlashAttribute("info", result.getInfo());
        return "redirect:/manage/link";
    }

    @RequestMapping(value = "/link/delete/{linkId}", method = RequestMethod.GET)
    public String deleteLink(@PathVariable("linkId") Integer linkId, RedirectAttributes attributes) {
        OperationResult result = linkService.deleteLink(linkId);
        attributes.addFlashAttribute("info", result.getInfo());
        return "redirect:/manage/category";
    }

    //对图片的管理
    @RequestMapping(value = "/image", method = {RequestMethod.GET, RequestMethod.POST})
    public String showImagesAndUpload(HttpServletRequest request, Model model) {
        String imageBasePath = request.getServletContext().getRealPath("images");
        processUpload(request, imageBasePath);

        String[] userImages = ImagesUtil.getImages(imageBasePath + File.separator + "user");
        String[] articleImages = ImagesUtil.getImages(imageBasePath + File.separator + "article");
        model.addAttribute("userImages", userImages);
        model.addAttribute("articleImages", articleImages);
        model.addAttribute("mainPage", "image.jsp");
        return "manage/manage";
    }

    private void processUpload(HttpServletRequest request, String imageBasePath) {
        System.out.println("process upload");
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = null;
        try {
            items = upload.parseRequest(request);
        } catch (FileUploadException e) {
            return;
//			e.printStackTrace();
        }
        if (items == null) {
            return;
        }
        Iterator<FileItem> itr = items.iterator();
        while (itr.hasNext()) {
            FileItem item = (FileItem) itr.next();
            if (item.isFormField()) {
                System.out.println("error");
            } else if ("userImage".equals(item.getFieldName())) {
                try {
                    String imageName = item.getName();
                    String filePath = imageBasePath + File.separator
                            + "user"
                            + File.separator + imageName;
                    System.out.println(filePath);
                    item.write(new File(filePath));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ("articleImage".equals(item.getFieldName())) {
                try {
                    String imageName = item.getName();
                    String filePath = imageBasePath + File.separator
                            + "article"
                            + File.separator + imageName;
                    System.out.println(filePath);
                    item.write(new File(filePath));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //对于关于的管理
    @RequestMapping(value = "/about", method = RequestMethod.GET)
    public String showAbout(Model model) {
        model.addAttribute("about", aboutService.getAbout());
        model.addAttribute("mainPage", "about.jsp");
        return "manage/manage";
    }

    @RequestMapping(value = "/about/modify", method = RequestMethod.GET)
    public String modifyAbout(HttpServletRequest request, Model model, RedirectAttributes attributes) {
        model.addAttribute("about", aboutService.getAbout());
        model.addAttribute("mainPage", "aboutEditor.jsp");
        return "manage/manage";
    }

    @RequestMapping(value = "/about/save", method = RequestMethod.POST)
    public String saveAbout(HttpServletRequest request, RedirectAttributes attributes) {
        String content = request.getParameter("content");
        OperationResult result = aboutService.updateAbout(content);
        attributes.addFlashAttribute("info", result.getInfo());
        return "redirect:/manage/about";
    }

}