package com.evv;

import static com.evv.util.Util.DEFAULT_PAGE_SIZE;

public class TestUtils {

    public static final String FIRST_CLIENT_EMAIL = "test-client-email1@gmail.com";

    public static final String SECOND_CLIENT_EMAIL = "test-client-email2@yandex.ru";

    public static final String FIRST_ADMIN_EMAIL = "test-admin-email1@yandex.ru";

    public static final String MESSAGE_PRODUCTS_NOT_EMPTY = "The list of selected products must not be empty";

    // language=JSON
    public static final String JSON_1_PURCHASE = """
            {
              "id": 2,
              "client": {
                "id": 1,
                "email": "test-client-email1@gmail.com",
                "role": "CLIENT",
                "birthDate": "1974-06-15",
                "clientStatus": "ACTIVE",
                "image": "",
                "gender": null
              },
              "products": [
                {
                  "id": 4,
                  "name": "ВАЗ-2106",
                  "cost": 10000.0000
                },
                {
                  "id": 5,
                  "name": "Удочка бамбук",
                  "cost": 5000.0000
                }
              ],
              "payment": {
                "id": 3,
                "amount": 15000.0000,
                "account": {
                  "id": 1,
                  "balance": 25000.1000,
                  "clientId": 1
                }
              }
            }
            """;

    // language=JSON
    public static final String JSON_2_PURCHASES = """
            {
              "content": [
                {
                  "id": 1,
                  "client": {
                    "id": 1,
                    "email": "test-client-email1@gmail.com",
                    "role": "CLIENT",
                    "birthDate": "1974-06-15",
                    "clientStatus": "ACTIVE",
                    "image": "",
                    "gender": null
                  },
                  "products": [
                    {
                      "id": 5,
                      "name": "Удочка бамбук",
                      "cost": 5000.0000
                    },
                    {
                      "id": 7,
                      "name": "Удобрение клубника",
                      "cost": 2500.0000
                    }
                  ],
                  "payment": {
                    "id": 1,
                    "amount": 5000.0000,
                    "creditCard": {
                      "id": 1,
                      "expirationDate": "2026-01-01",
                      "balance": -1000.5000,
                      "creditLimit": -30000.0000,
                      "status": "ACTIVE",
                      "clientId": 1
                    }
                  }
                },
                {
                  "id": 2,
                  "client": {
                    "id": 1,
                    "email": "test-client-email1@gmail.com",
                    "role": "CLIENT",
                    "birthDate": "1974-06-15",
                    "clientStatus": "ACTIVE",
                    "image": "",
                    "gender": null
                  },
                  "products": [
                    {
                      "id": 4,
                      "name": "ВАЗ-2106",
                      "cost": 10000.0000
                    },
                    {
                      "id": 5,
                      "name": "Удочка бамбук",
                      "cost": 5000.0000
                    }
                  ],
                  "payment": {
                    "id": 3,
                    "amount": 15000.0000,
                    "account": {
                      "id": 1,
                      "balance": 25000.1000,
                      "clientId": 1
                    }
                  }
                }
              ],
              "metadata": {
                "page": 0,
                "size": 2,
                "totalElements": 3,
                "numberOfElements": 2
              }
            }
            """;

    // language=JSON
    public static final String JSON_2_PURCHASES_WITH_DEF_PAGE_SIZE = """
            {
              "content": [
                {
                  "id": 1,
                  "client": {
                    "id": 1,
                    "email": "test-client-email1@gmail.com",
                    "role": "CLIENT",
                    "birthDate": "1974-06-15",
                    "clientStatus": "ACTIVE",
                    "image": "",
                    "gender": null
                  },
                  "products": [
                    {
                      "id": 5,
                      "name": "Удочка бамбук",
                      "cost": 5000.0000
                    },
                    {
                      "id": 7,
                      "name": "Удобрение клубника",
                      "cost": 2500.0000
                    }
                  ],
                  "payment": {
                    "id": 1,
                    "amount": 5000.0000,
                    "creditCard": {
                      "id": 1,
                      "expirationDate": "2026-01-01",
                      "balance": -1000.5000,
                      "creditLimit": -30000.0000,
                      "status": "ACTIVE",
                      "clientId": 1
                    }
                  }
                },
                {
                  "id": 2,
                  "client": {
                    "id": 1,
                    "email": "test-client-email1@gmail.com",
                    "role": "CLIENT",
                    "birthDate": "1974-06-15",
                    "clientStatus": "ACTIVE",
                    "image": "",
                    "gender": null
                  },
                  "products": [
                    {
                      "id": 4,
                      "name": "ВАЗ-2106",
                      "cost": 10000.0000
                    },
                    {
                      "id": 5,
                      "name": "Удочка бамбук",
                      "cost": 5000.0000
                    }
                  ],
                  "payment": {
                    "id": 3,
                    "amount": 15000.0000,
                    "account": {
                      "id": 1,
                      "balance": 25000.1000,
                      "clientId": 1
                    }
                  }
                }
              ],
              "metadata": {
                "page": 0,
                "size": %d,
                "totalElements": 2,
                "numberOfElements": 2
              }
            }
            """.formatted(DEFAULT_PAGE_SIZE);

    // language=JSON
    public static final String JSON_3_PURCHASES_WITH_DEF_PAGE_SIZE = """
            {
               "content": [
                 {
                   "id": 1,
                   "client": {
                     "id": 1,
                     "email": "test-client-email1@gmail.com",
                     "role": "CLIENT",
                     "birthDate": "1974-06-15",
                     "clientStatus": "ACTIVE",
                     "image": "",
                     "gender": null
                   },
                   "products": [
                     {
                       "id": 5,
                       "name": "Удочка бамбук",
                       "cost": 5000.0000
                     },
                     {
                       "id": 7,
                       "name": "Удобрение клубника",
                       "cost": 2500.0000
                     }
                   ],
                   "payment": {
                     "id": 1,
                     "amount": 5000.0000,
                     "creditCard": {
                       "id": 1,
                       "expirationDate": "2026-01-01",
                       "balance": -1000.5000,
                       "creditLimit": -30000.0000,
                       "status": "ACTIVE",
                       "clientId": 1
                     }
                   }
                 },
                 {
                   "id": 2,
                   "client": {
                     "id": 1,
                     "email": "test-client-email1@gmail.com",
                     "role": "CLIENT",
                     "birthDate": "1974-06-15",
                     "clientStatus": "ACTIVE",
                     "image": "",
                     "gender": null
                   },
                   "products": [
                     {
                       "id": 5,
                       "name": "Удочка бамбук",
                       "cost": 5000.0000
                     },
                     {
                       "id": 4,
                       "name": "ВАЗ-2106",
                       "cost": 10000.0000
                     }
                   ],
                   "payment": {
                     "id": 3,
                     "amount": 15000.0000,
                     "account": {
                       "id": 1,
                       "balance": 25000.1000,
                       "clientId": 1
                     }
                   }
                 },
                 {
                   "id": 5,
                   "client": {
                     "id": 1,
                     "email": "test-client-email1@gmail.com",
                     "role": "CLIENT",
                     "birthDate": "1974-06-15",
                     "clientStatus": "ACTIVE",
                     "image": "",
                     "gender": null
                   },
                   "products": [
                     {
                       "id": 16,
                       "name": "Сплит-система sum-9",
                       "cost": 25000.0000
                     }
                   ],
                   "payment": {
                     "id": 5,
                     "amount": 25000.0000,
                     "creditCard": {
                       "id": 1,
                       "expirationDate": "2026-01-01",
                       "balance": -1000.5000,
                       "creditLimit": -30000.0000,
                       "status": "ACTIVE",
                       "clientId": 1
                     }
                   }
                 }
               ],
               "metadata": {
                 "page": 0,
                 "size": 12,
                 "totalElements": 3,
                 "numberOfElements": 3
               }
             }
            """;

    // language=JSON
    public static final String JSON_SECOND_PAGE_1_PURCHASE_WITH_PAGE_SIZE_2 = """
            {
                "content": [
                  {
                    "id": 5,
                    "client": {
                      "id": 1,
                      "email": "test-client-email1@gmail.com",
                      "role": "CLIENT",
                      "birthDate": "1974-06-15",
                      "clientStatus": "ACTIVE",
                      "image": "",
                      "gender": null
                    },
                    "products": [
                      {
                        "id": 16,
                        "name": "Сплит-система sum-9",
                        "cost": 25000.0000
                      }
                    ],
                    "payment": {
                      "id": 5,
                      "amount": 25000.0000,
                      "creditCard": {
                        "id": 1,
                        "expirationDate": "2026-01-01",
                        "balance": -1000.5000,
                        "creditLimit": -30000.0000,
                        "status": "ACTIVE",
                        "clientId": 1
                      }
                    }
                  }
                ],
                "metadata": {
                  "page": 1,
                  "size": 2,
                  "totalElements": 3,
                  "numberOfElements": 1
                }
              }
            """;

    // language=JSON
    public static final String JSON_4_PURCHASES = """
            {
              "content": [
                {
                  "id": 1,
                  "client": {
                    "id": 1,
                    "email": "test-client-email1@gmail.com",
                    "role": "CLIENT",
                    "birthDate": "1974-06-15",
                    "clientStatus": "ACTIVE",
                    "image": "",
                    "gender": null
                  },
                  "products": [
                    {
                      "id": 5,
                      "name": "Удочка бамбук",
                      "cost": 5000.0000
                    },
                    {
                      "id": 7,
                      "name": "Удобрение клубника",
                      "cost": 2500.0000
                    }
                  ],
                  "payment": {
                    "id": 1,
                    "amount": 5000.0000,
                    "creditCard": {
                      "id": 1,
                      "expirationDate": "2026-01-01",
                      "balance": -1000.5000,
                      "creditLimit": -30000.0000,
                      "status": "ACTIVE",
                      "clientId": 1
                    }
                  }
                },
                {
                  "id": 2,
                  "client": {
                    "id": 1,
                    "email": "test-client-email1@gmail.com",
                    "role": "CLIENT",
                    "birthDate": "1974-06-15",
                    "clientStatus": "ACTIVE",
                    "image": "",
                    "gender": null
                  },
                  "products": [
                    {
                      "id": 4,
                      "name": "ВАЗ-2106",
                      "cost": 10000.0000
                    },
                    {
                      "id": 5,
                      "name": "Удочка бамбук",
                      "cost": 5000.0000
                    }
                  ],
                  "payment": {
                    "id": 3,
                    "amount": 15000.0000,
                    "account": {
                      "id": 1,
                      "balance": 25000.1000,
                      "clientId": 1
                    }
                  }
                },
                {
                  "id": 3,
                  "client": {
                    "id": 2,
                    "email": "test-client-email2@yandex.ru",
                    "role": "CLIENT",
                    "birthDate": "1980-01-20",
                    "clientStatus": "BLOCKED",
                    "image": "",
                    "gender": "MALE"
                  },
                  "products": [
                    {
                      "id": 9,
                      "name": "Смартфон PX-400 4,5\\"",
                      "cost": 10000.0000
                    }
                  ],
                  "payment": {
                    "id": 2,
                    "amount": 10000.0000,
                    "creditCard": {
                      "id": 4,
                      "expirationDate": "2023-07-17",
                      "balance": -70000.0000,
                      "creditLimit": -50000.0000,
                      "status": "BLOCKED",
                      "clientId": 2
                    }
                  }
                },
                {
                  "id": 4,
                  "client": {
                    "id": 2,
                    "email": "test-client-email2@yandex.ru",
                    "role": "CLIENT",
                    "birthDate": "1980-01-20",
                    "clientStatus": "BLOCKED",
                    "image": "",
                    "gender": "MALE"
                  },
                  "products": [
                    {
                      "id": 1,
                      "name": "Смартфон PX-300 4\\"",
                      "cost": 20000.0000
                    }
                  ],
                  "payment": {
                    "id": 4,
                    "amount": 20000.0000,
                    "account": null
                  }
                }
              ],
              "metadata": {
                "page": 0,
                "size": 4,
                "totalElements": 5,
                "numberOfElements": 4
              }
            }
            """;

    // language=JSON
    public static final String JSON_NEW_CREDIT_CARD_PURCHASE = """
            {
              "id": 6,
              "client": {
                "id": 1,
                "email": "test-client-email1@gmail.com",
                "role": "CLIENT",
                "birthDate": "1974-06-15",
                "clientStatus": "ACTIVE",
                "image": "",
                "gender": null
              },
              "products": [
                {
                  "id": 4,
                  "name": "ВАЗ-2106",
                  "cost": 10000.0000
                },
                {
                  "id": 9,
                  "name": "Смартфон PX-400 4,5\\"",
                  "cost": 10000.0000
                },
                {
                  "id": 12,
                  "name": "ВАЗ-2107",
                  "cost": 15000.0000
                }
              ],
              "payment": {
                "id": 6,
                "amount": 8500,
                "creditCard": {
                  "id": 1,
                  "expirationDate": "2026-01-01",
                  "balance": -9500.5000,
                  "creditLimit": -30000.0000,
                  "status": "ACTIVE",
                  "clientId": 1
                }
              }
            }
            """;

    // language=JSON
    public static final String JSON_NEW_ACCOUNT_PURCHASE = """
            {
              "id": 6,
              "client": {
                "id": 1,
                "email": "test-client-email1@gmail.com",
                "role": "CLIENT",
                "birthDate": "1974-06-15",
                "clientStatus": "ACTIVE",
                "image": "",
                "gender": null
              },
              "products": [
                {
                  "id": 4,
                  "name": "ВАЗ-2106",
                  "cost": 10000.0000
                },
                {
                  "id": 9,
                  "name": "Смартфон PX-400 4,5\\"",
                  "cost": 10000.0000
                },
                {
                  "id": 12,
                  "name": "ВАЗ-2107",
                  "cost": 15000.0000
                }
              ],
              "payment": {
                "id": 6,
                "amount": 8500,
                "account": {
                  "id": 1,
                  "balance": 16500.1000,
                  "clientId": 1
                }
              }
            }
            """;

    // language=JSON
    public static final String JSON_UPDATED_PURCHASE_CC_TO_SAME_CC = """
            {
              "id": 1,
              "client": {
                "id": 1,
                "email": "test-client-email1@gmail.com",
                "role": "CLIENT",
                "birthDate": "1974-06-15",
                "clientStatus": "ACTIVE",
                "image": "",
                "gender": null
              },
              "products": [
                {
                  "id": 12,
                  "name": "ВАЗ-2107",
                  "cost": 15000.0000
                },
                {
                  "id": 9,
                  "name": "Смартфон PX-400 4,5\\"",
                  "cost": 10000.0000
                }
              ],
              "payment": {
                "id": 6,
                "amount": 8500,
                "creditCard": {
                  "id": 1,
                  "expirationDate": "2026-01-01",
                  "balance": -4500.5000,
                  "creditLimit": -30000.0000,
                  "status": "ACTIVE",
                  "clientId": 1
                }
              }
            }
            """;

    // language=JSON
    public static final String JSON_UPDATED_PURCHASE_CC_TO_OTHER_CC = """
            {
              "id": 1,
              "client": {
                "id": 1,
                "email": "test-client-email1@gmail.com",
                "role": "CLIENT",
                "birthDate": "1974-06-15",
                "clientStatus": "ACTIVE",
                "image": "",
                "gender": null
              },
              "products": [
                {
                  "id": 12,
                  "name": "ВАЗ-2107",
                  "cost": 15000.0000
                },
                {
                  "id": 9,
                  "name": "Смартфон PX-400 4,5\\"",
                  "cost": 10000.0000
                }
              ],
              "payment": {
                "id": 6,
                "amount": 8500,
                "creditCard": {
                  "id": 2,
                  "expirationDate": "2025-01-15",
                  "balance": -58500.0000,
                  "creditLimit": -50000.0000,
                  "status": "LIMIT_EXCEEDED",
                  "clientId": 1
                }
              }
            }
            """;

    // language=JSON
    public static final String JSON_UPDATED_PURCHASE_CC_TO_ACCOUNT = """
              {
              "id": 1,
              "client": {
                "id": 1,
                "email": "test-client-email1@gmail.com",
                "role": "CLIENT",
                "birthDate": "1974-06-15",
                "clientStatus": "ACTIVE",
                "image": "",
                "gender": null
              },
              "products": [
                {
                  "id": 12,
                  "name": "ВАЗ-2107",
                  "cost": 15000.0000
                },
                {
                  "id": 9,
                  "name": "Смартфон PX-400 4,5\\"",
                  "cost": 10000.0000
                }
              ],
              "payment": {
                "id": 6,
                "amount": 8500,
                "account": {
                  "id": 1,
                  "balance": 16500.1000,
                  "clientId": 1
                }
              }
            }
            """;

    // language=JSON
    public static final String JSON_UPDATED_PURCHASE_ACCOUNT_TO_SAME_ACCOUNT = """
            {
               "id": 2,
               "client": {
                 "id": 1,
                 "email": "test-client-email1@gmail.com",
                 "role": "CLIENT",
                 "birthDate": "1974-06-15",
                 "clientStatus": "ACTIVE",
                 "image": "",
                 "gender": null
               },
               "products": [
                 {
                   "id": 12,
                   "name": "ВАЗ-2107",
                   "cost": 15000.0000
                 },
                 {
                   "id": 9,
                   "name": "Смартфон PX-400 4,5\\"",
                   "cost": 10000.0000
                 }
               ],
               "payment": {
                 "id": 6,
                 "amount": 8500,
                 "account": {
                   "id": 1,
                   "balance": 31500.1000,
                   "clientId": 1
                 }
               }
             }
            """;

    // language=JSON
    public static final String JSON_UPDATED_PURCHASE_ACCOUNT_TO_OTHER_ACCOUNT = """
            {
              "id": 2,
              "client": {
                "id": 1,
                "email": "test-client-email1@gmail.com",
                "role": "CLIENT",
                "birthDate": "1974-06-15",
                "clientStatus": "ACTIVE",
                "image": "",
                "gender": null
              },
              "products": [
                {
                  "id": 12,
                  "name": "ВАЗ-2107",
                  "cost": 15000.0000
                },
                {
                  "id": 9,
                  "name": "Смартфон PX-400 4,5\\"",
                  "cost": 10000.0000
                }
              ],
              "payment": {
                "id": 6,
                "amount": 8500,
                "account": {
                  "id": 2,
                  "balance": 41500.0000,
                  "clientId": 2
                }
              }
            }
            """;

    // language=JSON
    public static final String JSON_UPDATED_PURCHASE_ACCOUNT_TO_CREDIT_CARD = """
            {
              "id": 2,
              "client": {
                "id": 1,
                "email": "test-client-email1@gmail.com",
                "role": "CLIENT",
                "birthDate": "1974-06-15",
                "clientStatus": "ACTIVE",
                "image": "",
                "gender": null
              },
              "products": [
                {
                  "id": 12,
                  "name": "ВАЗ-2107",
                  "cost": 15000.0000
                },
                {
                  "id": 9,
                  "name": "Смартфон PX-400 4,5\\"",
                  "cost": 10000.0000
                }
              ],
              "payment": {
                "id": 6,
                "amount": 8500,
                "creditCard": {
                  "id": 1,
                  "expirationDate": "2026-01-01",
                  "balance": -9500.5000,
                  "creditLimit": -30000.0000,
                  "status": "ACTIVE",
                  "clientId": 1
                }
              }
            }
            """;
}
