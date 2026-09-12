package com.saucedemo.config;

import com.saucedemo.model.Product;
import com.saucedemo.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    public DataSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        // "code" es el identificador estable de cada producto: NUNCA lo cambies,
        // porque es lo que usa el seeder para reconocer el producto entre reinicios.
        // "name", "description" (tarjeta del catalogo), "detailDescription" (pagina
        // de detalle del producto), "price" e "imageUrl" son editables cuando quieras:
        // se actualizan solos en la base de datos sin crear duplicados.
        List<Product> catalog = List.of(
                new Product("mochila", "Mochila Urban Trek", "Mochila resistente para el dia a dia.", "Diseñada para acompañarte en tu ritmo diario. Elaborada con materiales de alta resistencia, cuenta con compartimentos organizadores de fácil acceso e interiores amplios para llevar tus pertenencias con total comodidad y seguridad a donde vayas.", 119900.0, "Images/mochila.png"),
                new Product("lampara", "Luz LED Bici-Pro", "Luz LED para bicicleta.", "Ilumina tu camino y rueda con total seguridad. Ofrece un haz de luz potente y uniforme de bajo consumo, ideal para recorridos nocturnos o de baja visibilidad. Fácil de instalar y resistente al uso exterior.", 39900.0, "Images/lampara.png"),
                new Product("camiseta", "Camiseta Essential", "Camiseta de algodon 100%.", "La prenda infaltable en tu armario. Confeccionada con algodón 100% suave al tacto y transpirable, te ofrece frescura y un ajuste cómodo durante todo el día. Ideal para combinar con cualquier estilo.", 59900.0, "Images/camiseta.png"),
                new Product("chaqueta", "Chaqueta Thermal Shield", "Chaqueta abrigada para clima frio.", "Tu aliada perfecta para los días fríos. Cuenta con aislamiento térmico ligero que retiene el calor corporal sin restar movilidad. Su diseño moderno la hace ideal tanto para la ciudad como para salidas al aire libre.", 189900.0, "Images/chaqueta.png"),
                new Product("enterizo", "Baby Sleep Suit", "Enterizo comodo para dormir.", "Máximo confort para el descanso de tu bebé. Confeccionado en telas ultra suaves que protegen la piel delicada, con un diseño holgado que facilita los movimientos y broches prácticos para un cambio de ropa rápido.", 79900.0, "Images/enterizo.jpg"),
                new Product("camiseta-especial", "Camiseta Crimson Edition", "Camiseta edicion especial color rojo.", "Marca la diferencia con esta edición especial en tono rojo vibrante. Mantiene el confort y la suavidad del algodón premium, con un color duradero que resalta en cualquier combinación casual.", 65900.0, "Images/camiseta-especial.jpg"),
                new Product("gafas", "Gafas Solar Shade UV", "Gafas de sol con proteccion UV.", "Protege tus ojos con un toque de estilo. Cuentan con lentes de alta definición con filtro de protección solar UV que reducen los reflejos y garantizan una visión nítida y cómoda en días soleados.", 49900.0, "Images/gafas.jpg"),
                new Product("gorra", "Gorra Classic Fit", "Gorra ajustable de algodon.", "El complemento perfecto para un look relajado. Elaborada en algodón transpirable con correa ajustable en la parte posterior para un encaje personalizado. Resistente, ligera y fácil de llevar.", 34900.0, "Images/gorra.jpg"),
                new Product("botella", "Botella ThermoHydro", "Botella termica de 750ml.", "Mantén tus bebidas a la temperatura ideal por horas. Fabricada en acero con aislamiento térmico de doble capa, es a prueba de fugas y perfecta para llevar al gimnasio, la oficina o tus viajes.", 44900.0, "Images/botella.jpg"),
                new Product("medias", "Medias Daily Comfort", "Pack de 3 pares de medias.", "Comodidad paso a paso. Set de 3 pares de medias elaboradas en tejido elástico y acolchado en zonas clave para amortiguar la pisada y mantener tus pies frescos todo el día.", 24900.0, "Images/medias.jpg"),
                new Product("buso", "Buso Hoodie City", "Buso con capota, ideal para clima templado.", "La combinación ideal entre abrigo y estilo. Diseñado con capota ajustable y bolsillo frontal tipo canguro, confeccionado en tela suave al tacto para brindarte calidez en climas templados o fríos.", 149900.0, "Images/buso.jpg"),
                new Product("bolsa", "Eco Tote Bag", "Bolsa de tela reutilizable.", "Una alternativa práctica, duradera y ecológica. Elaborada en tela resistente reutilizable, perfecta para llevar tus compras, libros o esenciales diarios reduciendo el consumo de plásticos de un solo uso.", 39900.0, "Images/bolsa.jpg")
        );

        catalog.forEach(product -> {
            // Busca primero por "code" (identificador estable). Si no existe todavia
            // (bases de datos antiguas sin esta columna poblada), busca por el nombre
            // original -que hoy coincide con el code- para adoptar esa fila una sola vez.
            Product existing = productRepository.findByCode(product.getCode())
                    .or(() -> productRepository.findByName(product.getCode()))
                    .orElse(null);

            if (existing != null) {
                existing.setCode(product.getCode());
                existing.setName(product.getName());
                existing.setDescription(product.getDescription());
                existing.setDetailDescription(product.getDetailDescription());
                existing.setPrice(product.getPrice());
                existing.setImageUrl(product.getImageUrl());
                productRepository.save(existing);
            } else {
                productRepository.save(product);
            }
        });
    }
}
