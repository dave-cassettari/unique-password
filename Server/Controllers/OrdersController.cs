using System.Collections.Generic;
using System.Web.Http;

namespace UniquePassword.Server.Controllers
{
    [RoutePrefix("api/orders")]
    public class OrdersController : ApiController
    {
        //[Authorize]
        [Route]
        public IHttpActionResult Get()
        {
            return Json(Order.CreateOrders());
        }
    }

    public class Order
    {
        public int OrderID { get; set; }
        public bool IsShipped { get; set; }
        public string ShipperCity { get; set; }
        public string CustomerName { get; set; }

        public static ICollection<Order> CreateOrders()
        {
            return new List<Order>
            {
                new Order { OrderID = 10248, CustomerName = "Taiseer Joudeh", ShipperCity = "Amman", IsShipped = true },
                new Order { OrderID = 10249, CustomerName = "Ahmad Hasan", ShipperCity = "Dubai", IsShipped = false },
                new Order { OrderID = 10250, CustomerName = "Tamer Yaser", ShipperCity = "Jeddah", IsShipped = false },
                new Order { OrderID = 10251, CustomerName = "Lina Majed", ShipperCity = "Abu Dhabi", IsShipped = false },
                new Order { OrderID = 10252, CustomerName = "Yasmeen Rami", ShipperCity = "Kuwait", IsShipped = true }
            };
        }
    }
}