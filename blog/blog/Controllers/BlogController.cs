using System;
using System.Collections.Generic;
using System.Web.Mvc;
using blog.Models;
using blog.ViewModels;

namespace blog.Controllers
{
    [Route("api/blogs")]
    public class BlogController : Controller
    {
        private static List<Blog> blogs =  new List<Blog>{
            new Blog { Id = "1", Body = "body1", Title = "title1" },
            new Blog { Id = "2", Body = "body2", Title = "title2" },
            new Blog { Id = "3", Body = "body3", Title = "title3" },
        };
        
        [HttpGet]
        public JsonResult All()
        {   
            return Json(blogs, JsonRequestBehavior.AllowGet);
        }
        
        [HttpPost]
        public JsonResult Create(Blog blog)
        {   
            blog.Id = Guid.NewGuid().ToString();
            if (ModelState.IsValid)
            {
               blogs.Add(blog);
            }
            
            return Json(new {message = "blog created successfully", blogs = blogs
            });
        }
        
    }
}