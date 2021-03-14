package ecomm

import java.sql.{Date, Timestamp}

case class Transaction(order_id: Int,
                       order_ts: Timestamp,
                       order_date: Date,
                       customer_id: Int,
                       product_id: Int,
                       product_category: String,
                       product_price: Float,
                       product_discount_rate: Float,
                       product_quantity: Int,
                       product_created_date: Timestamp
                      )