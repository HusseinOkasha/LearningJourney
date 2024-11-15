using System.Collections.Generic;
using System.Web.Mvc;
using blog.Models;
using blog.ViewModels;

namespace blog.Controllers
{
    public class BlogController : Controller
    {
        // GET
        public ActionResult All()
        {   
            var blogs = new List<Blog>{
                new Blog { Id = "1", Body = "body1", Title = "title1" },
                new Blog { Id = "2", Body = "body2", Title = "title2" },
                new Blog { Id = "3", Body = "body3", Title = "title3" },
            };
            var viewModel = new BlogViewModel{Blogs = blogs};
            return View(viewModel);
        }
    }
}