package samples;

public sealed interface ModernJavaSample permits ModernJavaSample.Circle, ModernJavaSample.Square {

    record Circle(double radius) implements ModernJavaSample { }

    record Square(double side) implements ModernJavaSample { }

    static double area(ModernJavaSample shape) {
        return switch (shape) {
            case Circle c -> Math.PI * c.radius() * c.radius();
            case Square s -> s.side() * s.side();
        };
    }

    static String describe(Object obj) {
        if (obj instanceof Circle(double radius) && radius > 0) {
            return """
                    circle of radius %s
                    """.formatted(radius);
        }
        return "something else";
    }
}
