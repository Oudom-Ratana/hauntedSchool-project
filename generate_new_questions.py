import json
import os

def create_all_questions():
    questions = []

    # =========================================================================
    # CATEGORY 1: MATH (40 Questions - 20 Easy, 20 Medium)
    # Rooms: classroomA (20), basement (20)
    # =========================================================================
    math_classroomA = [
        # Easy (10)
        ("What is the sum of the interior angles of any triangle?", "90 degrees", "180 degrees", "270 degrees", "360 degrees", "B", "Easy", "The sum of interior angles in any Euclidean triangle is always 180 degrees."),
        ("What is the value of 15% of 200?", "20", "25", "30", "35", "C", "Easy", "15% of 200 is (15 / 100) * 200 = 30."),
        ("What is the square root of 144?", "10", "11", "12", "14", "C", "Easy", "12 * 12 = 144, so the square root is 12."),
        ("Solve for x: 3x + 9 = 24.", "3", "5", "7", "9", "B", "Easy", "Subtract 9: 3x = 15; divide by 3: x = 5."),
        ("Which of the following numbers is a prime number?", "9", "15", "17", "21", "C", "Easy", "17 has no positive divisors other than 1 and itself."),
        ("What is the perimeter of a rectangle with length 8 cm and width 5 cm?", "26 cm", "40 cm", "13 cm", "30 cm", "A", "Easy", "Perimeter = 2 * (length + width) = 2 * (8 + 5) = 26 cm."),
        ("If a fair 6-sided die is rolled, what is the probability of rolling an even number?", "1/6", "1/3", "1/2", "2/3", "C", "Easy", "The even numbers are 2, 4, 6 (3 out of 6 outcomes), which simplifies to 1/2."),
        ("What is the value of 7 squared (7^2)?", "42", "47", "49", "56", "C", "Easy", "7 * 7 = 49."),
        ("What is the greatest common divisor (GCD) of 12 and 18?", "3", "6", "9", "12", "B", "Easy", "The common factors of 12 and 18 are 1, 2, 3, and 6. The greatest is 6."),
        ("What is the result of 25 * 4 - 30?", "50", "60", "70", "80", "C", "Easy", "By order of operations: 25 * 4 = 100; 100 - 30 = 70."),

        # Medium (10)
        ("In a right-angled triangle, if legs are a = 6 and b = 8, what is the hypotenuse c?", "9", "10", "12", "14", "B", "Medium", "By Pythagorean theorem: c^2 = 6^2 + 8^2 = 36 + 64 = 100; c = 10."),
        ("Solve the quadratic equation x^2 - 5x + 6 = 0 for x.", "x = 1 or x = 6", "x = 2 or x = 3", "x = -2 or x = -3", "x = 0 or x = 5", "B", "Medium", "(x - 2)(x - 3) = 0 gives roots x = 2 and x = 3."),
        ("What is the area of a circle with a radius of 7 units? (Use pi = 22/7)", "44 sq units", "88 sq units", "154 sq units", "308 sq units", "C", "Medium", "Area = pi * r^2 = (22/7) * 49 = 154 sq units."),
        ("If log10(x) = 3, what is the value of x?", "30", "100", "300", "1000", "D", "Medium", "log10(x) = 3 means 10^3 = x, so x = 1000."),
        ("What is the next number in the geometric sequence: 3, 6, 12, 24, ...?", "36", "48", "60", "72", "B", "Medium", "Each term is multiplied by common ratio 2. 24 * 2 = 48."),
        ("What is the median of the data set: [4, 8, 3, 9, 7, 5, 2]?", "4", "5", "6", "7", "B", "Medium", "Arranging in ascending order: 2, 3, 4, 5, 7, 8, 9. The middle value is 5."),
        ("A product priced at $80 is on sale for 25% off. What is the sale price?", "$55", "$60", "$65", "$70", "B", "Medium", "25% of $80 is $20. Sale price = $80 - $20 = $60."),
        ("What is the slope of the line passing through (2, 3) and (6, 11)?", "1.5", "2", "2.5", "4", "B", "Medium", "Slope m = (11 - 3) / (6 - 2) = 8 / 4 = 2."),
        ("How many degrees are in each interior angle of a regular hexagon?", "108 degrees", "120 degrees", "135 degrees", "144 degrees", "B", "Medium", "Interior angle = (n - 2) * 180 / n = 4 * 180 / 6 = 120 degrees."),
        ("What is the value of 5! (5 factorial)?", "60", "100", "120", "720", "C", "Medium", "5! = 5 * 4 * 3 * 2 * 1 = 120.")
    ]

    math_basement = [
        # Easy (10)
        ("What is 1/2 + 1/4?", "2/6", "3/4", "1/8", "2/4", "B", "Easy", "1/2 = 2/4. Adding 1/4 yields 3/4."),
        ("What is the Roman numeral for 50?", "C", "D", "L", "X", "C", "Easy", "L represents 50 in Roman numerals (C is 100, D is 500, X is 10)."),
        ("What is the average (mean) of 10, 20, and 30?", "15", "20", "25", "30", "B", "Easy", "Mean = (10 + 20 + 30) / 3 = 60 / 3 = 20."),
        ("How many sides does a regular decagon have?", "8", "9", "10", "12", "C", "Easy", "A decagon has exactly 10 sides."),
        ("What is 8 * 9?", "64", "71", "72", "81", "C", "Easy", "8 * 9 = 72."),
        ("If a train travels 60 miles per hour, how far does it travel in 3.5 hours?", "180 miles", "210 miles", "240 miles", "270 miles", "B", "Easy", "Distance = speed * time = 60 * 3.5 = 210 miles."),
        ("What is 10 to the power of 4 (10^4)?", "1,000", "10,000", "100,000", "1,000,000", "B", "Easy", "10^4 = 10,000."),
        ("Which angle is classified as an obtuse angle?", "45 degrees", "90 degrees", "125 degrees", "180 degrees", "C", "Easy", "An obtuse angle is strictly between 90 and 180 degrees. 125 degrees is obtuse."),
        ("What is 40% of 150?", "45", "50", "60", "75", "C", "Easy", "0.40 * 150 = 60."),
        ("What is the absolute value of -42?", "-42", "0", "42", "84", "C", "Easy", "Absolute value | -42 | is 42."),

        # Medium (10)
        ("What is the volume of a cylinder with radius r = 3 and height h = 7? (Use pi = 22/7)", "132", "198", "231", "264", "B", "Medium", "Volume = pi * r^2 * h = (22/7) * 9 * 7 = 198 cubic units."),
        ("If 2^(x+1) = 32, what is the value of x?", "3", "4", "5", "6", "B", "Medium", "32 = 2^5. So x + 1 = 5, which means x = 4."),
        ("What is the surface area of a cube with edge length 5 cm?", "125 cm^2", "150 cm^2", "175 cm^2", "200 cm^2", "B", "Medium", "Surface area = 6 * a^2 = 6 * 25 = 150 cm^2."),
        ("In how many different ways can 4 books be arranged on a shelf?", "12", "16", "24", "48", "C", "Medium", "Number of permutations = 4! = 4 * 3 * 2 * 1 = 24."),
        ("If a card is drawn from a standard 52-card deck, what is the probability of drawing an Ace?", "1/13", "1/26", "1/52", "4/13", "A", "Medium", "There are 4 Aces in 52 cards, so probability is 4/52 = 1/13."),
        ("What is the distance between points (1, 2) and (4, 6)?", "4", "5", "6", "7", "B", "Medium", "Distance = sqrt((4-1)^2 + (6-2)^2) = sqrt(9 + 16) = sqrt(25) = 5."),
        ("What is the sum of the first 10 positive integers (1 + 2 + ... + 10)?", "45", "50", "55", "60", "C", "Medium", "Sum = n(n + 1)/2 = 10 * 11 / 2 = 55."),
        ("If f(x) = 2x^2 - 3x + 1, what is f(3)?", "8", "10", "12", "14", "B", "Medium", "f(3) = 2(9) - 3(3) + 1 = 18 - 9 + 1 = 10."),
        ("What is the simple interest on a principal of $1,000 at 5% annual rate for 3 years?", "$50", "$100", "$150", "$200", "C", "Medium", "Interest = P * R * T = 1000 * 0.05 * 3 = $150."),
        ("Simplify: (x^3 * x^4) / x^2.", "x^4", "x^5", "x^6", "x^7", "B", "Medium", "x^(3+4) / x^2 = x^7 / x^2 = x^(7-2) = x^5.")
    ]

    # Build Math Questions
    for i, q in enumerate(math_classroomA, 1):
        task_num = ((i - 1) % 5) + 1
        questions.append({
            "id": f"Q_MAT_A_{i:03d}",
            "text": q[0], "optionA": q[1], "optionB": q[2], "optionC": q[3], "optionD": q[4],
            "correctAnswer": q[5], "category": "Math", "room": "classroomA",
            "difficulty": q[6], "rewardType": "Key" if i == 5 else "Item",
            "rewardValue": "Classroom B Key" if i == 5 else "Battery",
            "explanation": q[7], "active": True, "questionType": "MCQ", "taskNumber": task_num
        })

    for i, q in enumerate(math_basement, 1):
        task_num = ((i - 1) % 5) + 1
        questions.append({
            "id": f"Q_MAT_B_{i:03d}",
            "text": q[0], "optionA": q[1], "optionB": q[2], "optionC": q[3], "optionD": q[4],
            "correctAnswer": q[5], "category": "Math", "room": "basement",
            "difficulty": q[6], "rewardType": "Item", "rewardValue": "Toolbox",
            "explanation": q[7], "active": True, "questionType": "MCQ", "taskNumber": task_num
        })

    # =========================================================================
    # CATEGORY 2: SCIENCE (40 Questions - 20 Easy, 20 Medium)
    # Rooms: laboratory (20), dormitory (20)
    # =========================================================================
    science_lab = [
        # Easy (10)
        ("What is the chemical formula for water?", "CO2", "H2O", "NaCl", "O2", "B", "Easy", "Water consists of two Hydrogen atoms and one Oxygen atom (H2O)."),
        ("What is the center of an atom called?", "Electron", "Nucleus", "Proton", "Neutron", "B", "Easy", "The nucleus is the small, dense region consisting of protons and neutrons at the center of an atom."),
        ("What is the primary gas that plants absorb from the atmosphere for photosynthesis?", "Oxygen", "Nitrogen", "Carbon Dioxide", "Hydrogen", "C", "Easy", "Plants take in Carbon Dioxide (CO2) and release Oxygen during photosynthesis."),
        ("What is the chemical symbol for Gold on the periodic table?", "Ag", "Au", "Fe", "Pb", "B", "Easy", "Au comes from the Latin word 'Aurum', meaning gold."),
        ("Which planet is known as the Red Planet in our solar system?", "Venus", "Mars", "Jupiter", "Saturn", "B", "Easy", "Mars appears reddish due to iron oxide (rust) on its surface."),
        ("What is the freezing point of pure water at standard atmospheric pressure in Celsius?", "-10 C", "0 C", "32 C", "100 C", "B", "Easy", "Pure water freezes at 0 degrees Celsius (32 degrees Fahrenheit)."),
        ("What organelle is famously known as the powerhouse of the cell?", "Nucleus", "Ribosome", "Mitochondria", "Golgi apparatus", "C", "Easy", "Mitochondria generate most of the chemical energy needed to power the cell (ATP)."),
        ("What force pulls objects toward the center of the Earth?", "Magnetism", "Friction", "Gravity", "Centrifugal force", "C", "Easy", "Gravity is the fundamental force of attraction between masses."),
        ("What state of matter has a definite volume but no fixed shape?", "Solid", "Liquid", "Gas", "Plasma", "B", "Easy", "Liquids conform to the shape of their container while retaining constant volume."),
        ("What is the pH value of pure neutral water at 25 degrees Celsius?", "0", "5", "7", "14", "C", "Easy", "A pH of 7 represents a completely neutral solution on the 0-14 pH scale."),

        # Medium (10)
        ("What is the acceleration due to Earth's gravity near sea level?", "8.9 m/s^2", "9.8 m/s^2", "10.8 m/s^2", "12.0 m/s^2", "B", "Medium", "Standard acceleration due to Earth gravity is approximately 9.8 m/s^2."),
        ("Which element has atomic number 6 on the periodic table?", "Nitrogen", "Carbon", "Boron", "Oxygen", "B", "Medium", "Carbon has 6 protons and an atomic number of 6."),
        ("What type of chemical bond involves the sharing of electron pairs between atoms?", "Ionic bond", "Covalent bond", "Hydrogen bond", "Metallic bond", "B", "Medium", "A covalent bond forms when two atoms share valence electrons."),
        ("What is the SI unit of electrical resistance?", "Volt", "Ampere", "Ohm", "Watt", "C", "Medium", "The Ohm (symbol: Omega) is the standard SI unit of electrical resistance."),
        ("According to Newton's Second Law of Motion, what is the formula for force?", "F = m / a", "F = m * a", "F = m * v", "F = 1/2 m * v^2", "B", "Medium", "Force equals mass multiplied by acceleration (F = ma)."),
        ("What type of lens is thinner in the center and causes light rays to diverge?", "Convex lens", "Concave lens", "Bifocal lens", "Cylindrical lens", "B", "Medium", "A concave lens is thinner at the center and spreads (diverges) parallel light rays."),
        ("What is the speed of light in a vacuum (approximate)?", "30,000 km/s", "150,000 km/s", "300,000 km/s", "1,000,000 km/s", "C", "Medium", "Light travels at approximately 299,792 km/s (~300,000 km/s) in vacuum."),
        ("Which blood component is primarily responsible for blood clotting to stop bleeding?", "Red blood cells", "White blood cells", "Platelets", "Hemoglobin", "C", "Medium", "Platelets (thrombocytes) aggregate and form clots at wound sites."),
        ("What is an exothermic reaction?", "A reaction that absorbs thermal energy", "A reaction that releases thermal energy", "A reaction requiring electricity", "A reaction that halts without oxygen", "B", "Medium", "Exothermic reactions release energy, usually in the form of heat or light."),
        ("Which law states that energy cannot be created or destroyed, only transformed?", "First Law of Thermodynamics", "Second Law of Thermodynamics", "Hooke's Law", "Boyle's Law", "A", "Medium", "The First Law of Thermodynamics is the principle of conservation of energy.")
    ]

    science_dormitory = [
        # Easy (10)
        ("How many bones are in an adult human skeleton?", "186", "206", "226", "256", "B", "Easy", "An adult human body has 206 bones."),
        ("Which organ pumps oxygenated blood throughout the human body?", "Lungs", "Liver", "Heart", "Kidneys", "C", "Easy", "The heart is the muscular organ that pumps blood through the circulatory system."),
        ("What is the largest organ of the human body by surface area?", "Liver", "Skin", "Brain", "Large Intestine", "B", "Easy", "Skin (the integumentary system) is the human body's largest organ."),
        ("What gas do humans inhale for cellular respiration?", "Carbon dioxide", "Helium", "Oxygen", "Nitrogen", "C", "Easy", "Oxygen is essential for human cellular respiration to generate energy."),
        ("Which vitamin is synthesized by human skin when exposed to sunlight?", "Vitamin A", "Vitamin B12", "Vitamin C", "Vitamin D", "D", "Easy", "Sunlight ultraviolet B (UVB) rays trigger the synthesis of Vitamin D in human skin."),
        ("What is the normal resting body temperature of a healthy human in Celsius?", "35.0 C", "37.0 C", "39.5 C", "41.0 C", "B", "Easy", "Normal average human body temperature is approximately 37.0 degrees Celsius (98.6 F)."),
        ("What mineral is vital for healthy bones and teeth?", "Iron", "Calcium", "Zinc", "Sodium", "B", "Easy", "Calcium is the primary building mineral stored in bones and teeth."),
        ("Which sense organ contains the cochlea responsible for hearing?", "Eye", "Ear", "Nose", "Tongue", "B", "Easy", "The cochlea is the spiral cavity of the inner ear containing auditory sensory cells."),
        ("What protein in red blood cells binds and transports oxygen?", "Hemoglobin", "Keratin", "Collagen", "Insulin", "A", "Easy", "Hemoglobin is the iron-containing oxygen-transport metalloprotein in red blood cells."),
        ("What part of the brain controls balance and fine motor coordination?", "Cerebrum", "Cerebellum", "Brainstem", "Thalamus", "B", "Easy", "The cerebellum coordinates voluntary movements, balance, and posture."),

        # Medium (10)
        ("Which blood type is considered the universal red blood cell donor?", "A positive", "B negative", "AB positive", "O negative", "D", "Medium", "Type O negative red blood cells lack A, B, and Rh antigens, making them universally acceptable."),
        ("What hormone produced by the pancreas regulates blood glucose levels?", "Thyroxine", "Adrenaline", "Insulin", "Cortisol", "C", "Medium", "Insulin allows cells to absorb glucose from the bloodstream."),
        ("What is the functional biological unit of the human kidney?", "Nephron", "Neuron", "Alveolus", "Villus", "A", "Medium", "Nephrons filter blood and produce urine in the kidneys."),
        ("Which division of the autonomic nervous system triggers the 'fight or flight' response?", "Parasympathetic", "Sympathetic", "Enteric", "Somatic", "B", "Medium", "The sympathetic nervous system accelerates heart rate and prepares the body for action."),
        ("Where in the human digestive system does the majority of nutrient absorption occur?", "Stomach", "Small Intestine", "Large Intestine", "Esophagus", "B", "Medium", "The small intestine (duodenum, jejunum, ileum) absorbs over 90% of nutrients."),
        ("What pathogen causes the common cold and seasonal flu?", "Bacteria", "Virus", "Fungus", "Protozoa", "B", "Medium", "Colds and influenza are caused by viral infections (rhinoviruses and influenza viruses)."),
        ("What is the primary function of white blood cells (leukocytes)?", "Carry oxygen", "Form scabs", "Defend against pathogens and infection", "Regulate body temperature", "C", "Medium", "White blood cells are core components of the immune system fighting pathogens."),
        ("What genetic molecule carries hereditary instructions for living organisms?", "RNA", "DNA", "ATP", "Lipids", "B", "Medium", "Deoxyribonucleic acid (DNA) stores genetic instructions for development and functioning."),
        ("Which blood vessels carry oxygen-depleted blood back toward the heart from tissues?", "Arteries", "Veins", "Capillaries", "Arterioles", "B", "Medium", "Veins carry deoxygenated blood back to the heart (with the exception of pulmonary veins)."),
        ("What type of joint is found at the human shoulder and hip, allowing multi-axis movement?", "Hinge joint", "Ball-and-socket joint", "Pivot joint", "Saddle joint", "B", "Medium", "Ball-and-socket joints permit rotational movement in all planes.")
    ]

    # Build Science Questions
    for i, q in enumerate(science_lab, 1):
        task_num = ((i - 1) % 5) + 1
        questions.append({
            "id": f"Q_SCI_A_{i:03d}",
            "text": q[0], "optionA": q[1], "optionB": q[2], "optionC": q[3], "optionD": q[4],
            "correctAnswer": q[5], "category": "Science", "room": "laboratory",
            "difficulty": q[6], "rewardType": "Item", "rewardValue": "Acid Bottle" if i == 5 else "Battery",
            "explanation": q[7], "active": True, "questionType": "MCQ", "taskNumber": task_num
        })

    for i, q in enumerate(science_dormitory, 1):
        task_num = ((i - 1) % 5) + 1
        questions.append({
            "id": f"Q_SCI_B_{i:03d}",
            "text": q[0], "optionA": q[1], "optionB": q[2], "optionC": q[3], "optionD": q[4],
            "correctAnswer": q[5], "category": "Science", "room": "dormitory",
            "difficulty": q[6], "rewardType": "Health" if i % 2 == 0 else "Item",
            "rewardValue": "First Aid Kit" if i % 2 == 0 else "Medicine Bottle",
            "explanation": q[7], "active": True, "questionType": "MCQ", "taskNumber": task_num
        })

    # =========================================================================
    # CATEGORY 3: CODING (JAVA, OOP) (40 Questions - 20 Easy, 20 Medium)
    # Rooms: computer (20), teacher (20)
    # =========================================================================
    coding_computer = [
        # Easy (10)
        ("Which keyword in Java is used to inherit from a superclass?", "implements", "extends", "inherits", "instanceof", "B", "Easy", "The 'extends' keyword establishes class inheritance in Java."),
        ("What is the entry point method signature for a standalone Java application?", "public void start()", "public static void main(String[] args)", "public run(String args)", "static void execute()", "B", "Easy", "Java applications begin execution at public static void main(String[] args)."),
        ("What is the default value of an uninitialized boolean field in a Java class?", "true", "false", "null", "0", "B", "Easy", "Primitive boolean class fields default to false."),
        ("Which of the following is NOT a primitive data type in Java?", "int", "char", "String", "double", "C", "Easy", "String is an immutable reference class, not a primitive type."),
        ("Which OOP pillar bundles data fields and methods together while restricting direct access?", "Inheritance", "Encapsulation", "Polymorphism", "Abstraction", "B", "Easy", "Encapsulation hides internal state and exposes access through public methods."),
        ("What keyword is used to create a new instance of an object in Java?", "create", "make", "new", "instantiate", "C", "Easy", "The 'new' keyword dynamically allocates memory for an object on the heap."),
        ("What does the 'final' keyword on a variable in Java signify?", "The variable is static", "The value cannot be reassigned once initialized", "The variable is garbage collected immediately", "The variable is private", "B", "Easy", "A final variable's value cannot be modified after assignment."),
        ("Which Java collection interface guarantees that elements are unique without duplicates?", "List", "Queue", "Set", "Map", "C", "Easy", "A Set is a collection that contains no duplicate elements."),
        ("What keyword in Java is used to invoke a parent class constructor or method?", "this", "super", "parent", "base", "B", "Easy", "'super' refers directly to the immediate superclass members or constructor."),
        ("What symbol is used for single-line comments in Java source code?", "#", "//", "/*", "--", "B", "Easy", "// designates the start of a single-line comment in Java."),

        # Medium (10)
        ("What is the key difference between method overloading and method overriding?", "Overloading occurs at runtime; Overriding occurs at compile-time", "Overloading has the same name with different parameters; Overriding re-declares the same signature in a subclass", "They are identical concepts in Java", "Overriding only works for static methods", "B", "Medium", "Overloading creates methods with same name but different signatures; overriding replaces inherited behavior."),
        ("Which memory area in the JVM stores dynamically allocated objects and instance variables?", "Stack", "Heap", "Method Area", "Program Counter", "B", "Medium", "The Java Virtual Machine allocates memory for all class instances and arrays in the Heap."),
        ("What is the purpose of an abstract class in Java?", "It can never have concrete methods", "It provides a common base blueprint and cannot be directly instantiated", "It guarantees all methods are static", "It prevents subclasses from inheriting", "B", "Medium", "Abstract classes serve as base classes that cannot be instantiated on their own."),
        ("Which keyword is used in a method header to declare that it may throw checked exceptions?", "try", "catch", "throws", "throw", "C", "Medium", "The 'throws' keyword in a method declaration specifies exceptions that callers must handle."),
        ("What does the Java 'this' keyword represent?", "A reference to the current object instance", "A reference to the parent class", "A pointer to the JVM", "A static method caller", "A", "Medium", "'this' refers to the current class instance invoking the method or constructor."),
        ("Which interface must be implemented to sort objects using java.util.Collections.sort() naturally?", "Comparator<T>", "Comparable<T>", "Cloneable", "Serializable", "B", "Medium", "Comparable<T> requires implementing compareTo(T o) for natural sorting."),
        ("What is the output of comparing two Strings using '==' instead of '.equals()'?", "'==' compares memory addresses (object references); '.equals()' compares character contents", "They always produce identical results", "'.equals()' compares string length only", "'==' is faster and compares contents", "A", "Medium", "'==' checks reference equality while equals() checks character content equality."),
        ("What is polymorphism in Java Object-Oriented Programming?", "Compiling code for multiple operating systems", "The ability of an object to take on many forms and execute subclass-specific methods", "Converting integers to floats", "Packaging code into JAR files", "B", "Medium", "Polymorphism allows subclasses to provide distinct implementations of shared parent types."),
        ("Can an interface in Java 8 and later contain concrete methods with bodies?", "No, interfaces can never have method bodies", "Yes, using the 'default' or 'static' keywords", "Only if the interface is marked abstract", "Only private static methods", "B", "Medium", "Java 8 introduced default and static interface methods with concrete bodies."),
        ("What block of code in a try-catch-finally statement ALWAYS executes?", "try block", "catch block", "finally block", "None of the above", "C", "Medium", "The finally block always executes whether an exception is caught, handled, or unhandled.")
    ]

    coding_teacher = [
        # Easy (10)
        ("Which access modifier makes a class member accessible ONLY within its own class?", "public", "protected", "private", "default (package-private)", "C", "Easy", "Private members cannot be accessed outside the class in which they are declared."),
        ("What is the index of the very first element in a standard Java array?", "0", "1", "-1", "null", "A", "Easy", "Java arrays are zero-indexed, meaning the first element is at index 0."),
        ("Which keyword is used to implement an interface in a Java class?", "inherits", "extends", "implements", "uses", "C", "Easy", "Classes use 'implements' to satisfy interface contracts."),
        ("What is a constructor in Java?", "A special method called to initialize a new object", "A method that destroys objects", "A tool for compiling source code", "A reserved variable name", "A", "Easy", "A constructor has the same name as the class and initializes newly allocated objects."),
        ("Which operator is used to test logical equality of values in Java?", "=", "==", "===", "!=", "B", "Easy", "The '==' operator evaluates whether two primitive values or references are equal."),
        ("What is the wrapper class for the primitive type 'int' in Java?", "Int", "Integer", "Number", "Int32", "B", "Easy", "java.lang.Integer is the object wrapper class for primitive int."),
        ("Which loop construct evaluates its boolean condition AFTER executing the loop body at least once?", "for loop", "while loop", "do-while loop", "enhanced for loop", "C", "Easy", "A do-while loop always executes its body at least once before checking the condition."),
        ("What keyword stops the execution of a loop immediately and transfers control out of it?", "continue", "skip", "break", "return", "C", "Easy", "The 'break' keyword immediately terminates the enclosing loop."),
        ("What does JVM stand for?", "Java Virtual Machine", "Java Variable Manager", "Java Visual Monitor", "Joint Vector Module", "A", "Easy", "The Java Virtual Machine executes compiled bytecode (.class files)."),
        ("What is the return type of a method that does not return any value?", "null", "empty", "void", "zero", "C", "Easy", "The 'void' return type specifies that the method returns no value."),

        # Medium (10)
        ("What design pattern restricts class instantiation to a single unique instance application-wide?", "Factory Pattern", "Singleton Pattern", "Observer Pattern", "Strategy Pattern", "B", "Medium", "Singleton ensures only one instance of a class exists throughout runtime."),
        ("What is autoboxing and unboxing in modern Java?", "Automatic memory garbage collection", "Automatic conversion between primitive types and their wrapper classes", "Compressing files into JAR format", "Compiling Java to C++", "B", "Medium", "Autoboxing automatically converts primitive types to wrapper objects and vice-versa."),
        ("What happens to an object when there are no longer any active references pointing to it?", "It causes a memory leak crash", "It becomes eligible for Garbage Collection", "It is automatically saved to disk", "It becomes static", "B", "Medium", "Unreferenced objects on the heap are identified and reclaimed by the Garbage Collector."),
        ("Why are Strings considered immutable in Java?", "Their contents cannot be changed after creation; modifications return new String instances", "They can only hold 256 characters", "They cannot be used in loops", "They are stored on the stack only", "A", "Medium", "Java Strings cannot be modified once instantiated, which ensures thread safety and security."),
        ("What is the difference between ArrayList and LinkedList in Java?", "ArrayList uses a dynamic resizable array; LinkedList uses doubly-linked node pointers", "ArrayList cannot store objects", "LinkedList is always slower in all operations", "They are identical classes", "A", "Medium", "ArrayList offers fast O(1) random index access; LinkedList provides fast O(1) insertions/deletions at ends."),
        ("What is the purpose of the '@Override' annotation in Java?", "It compiles code into machine code", "It signals the compiler that the method is intended to override a superclass method", "It creates a new thread", "It prevents subclassing", "B", "Medium", "The @Override annotation helps prevent bugs by verifying identical method signatures at compile-time."),
        ("Which class in Java is the ultimate root ancestor of every class hierarchy?", "java.lang.Class", "java.lang.System", "java.lang.Object", "java.lang.Root", "C", "Medium", "Every class in Java implicitly inherits directly or indirectly from java.lang.Object."),
        ("What is the difference between 'throw' and 'throws' keywords in Java?", "'throw' explicitly triggers an exception; 'throws' declares exceptions in a method signature", "'throws' is for runtime exceptions only", "They are interchangeable", "'throw' is a variable type", "A", "Medium", "'throw new Exception()' throws an instance; 'throws' declares potential exceptions in method signatures."),
        ("What is the purpose of generics (e.g. List<String>) introduced in Java 5?", "To allow classes to run faster on GPU", "To enforce compile-time type safety and eliminate runtime ClassCastExceptions", "To allow code to run without JVM", "To enable multiple class inheritance", "B", "Medium", "Generics provide compile-time type checking and remove the need for manual type casting."),
        ("What is the time complexity of looking up a key in a well-distributed Java HashMap?", "O(n)", "O(log n)", "O(1)", "O(n^2)", "C", "Medium", "Average-case lookup and insertion time in a balanced HashMap is O(1) constant time.")
    ]

    # Build Coding Questions
    for i, q in enumerate(coding_computer, 1):
        task_num = ((i - 1) % 5) + 1
        questions.append({
            "id": f"Q_COD_A_{i:03d}",
            "text": q[0], "optionA": q[1], "optionB": q[2], "optionC": q[3], "optionD": q[4],
            "correctAnswer": q[5], "category": "Coding (Java, OOP)", "room": "computer",
            "difficulty": q[6], "rewardType": "Key" if i == 5 else "Item",
            "rewardValue": "Night Vision Goggles" if i == 5 else "Flashlight",
            "explanation": q[7], "active": True, "questionType": "MCQ", "taskNumber": task_num
        })

    for i, q in enumerate(coding_teacher, 1):
        task_num = ((i - 1) % 5) + 1
        questions.append({
            "id": f"Q_COD_B_{i:03d}",
            "text": q[0], "optionA": q[1], "optionB": q[2], "optionC": q[3], "optionD": q[4],
            "correctAnswer": q[5], "category": "Coding (Java, OOP)", "room": "teacher",
            "difficulty": q[6], "rewardType": "Key" if i == 1 else "Item",
            "rewardValue": "Master Key" if i == 1 else "Ancient Tome",
            "explanation": q[7], "active": True, "questionType": "MCQ", "taskNumber": task_num
        })

    # =========================================================================
    # CATEGORY 4: NETWORKING (40 Questions - 20 Easy, 20 Medium)
    # Rooms: classroomB (20), entrance (20)
    # =========================================================================
    networking_classroomB = [
        # Easy (10)
        ("What does IP stand for in computer networking?", "Internet Protocol", "Internal Program", "Interconnect Point", "Interface Port", "A", "Easy", "Internet Protocol (IP) provides addressing and routing of packets across networks."),
        ("Which layer of the OSI model handles end-to-end data transport and reliability?", "Data Link Layer", "Network Layer", "Transport Layer", "Session Layer", "C", "Easy", "Layer 4 (Transport Layer) manages end-to-end connections, flow control, and error recovery."),
        ("What is the standard port number for unencrypted HTTP web traffic?", "21", "22", "80", "443", "C", "Easy", "Port 80 is the default standard port for HTTP traffic."),
        ("What is the default port number for secure HTTPS web traffic?", "80", "443", "8080", "8443", "B", "Easy", "Port 443 is standard for encrypted HTTPS (SSL/TLS) traffic."),
        ("What protocol automatically assigns dynamic IP addresses to devices on a network?", "DNS", "DHCP", "SNMP", "FTP", "B", "Easy", "Dynamic Host Configuration Protocol (DHCP) automatically assigns IP configuration leases."),
        ("What does DNS stand for?", "Data Network Service", "Domain Name System", "Dynamic Node Server", "Digital Name Storage", "B", "Easy", "Domain Name System (DNS) resolves human-friendly domain names to numeric IP addresses."),
        ("Which command-line utility is used to test network connectivity using ICMP packets?", "ping", "netstat", "ipconfig", "nslookup", "A", "Easy", "The 'ping' tool sends ICMP Echo Request packets to check reachability and latency."),
        ("What is the standard IPv4 loopback address referring to 'localhost'?", "192.168.1.1", "10.0.0.1", "127.0.0.1", "0.0.0.0", "C", "Easy", "127.0.0.1 is the standard IPv4 loopback address."),
        ("What physical address is permanently burned into a Network Interface Card (NIC)?", "IP Address", "MAC Address", "Port Number", "Subnet Mask", "B", "Easy", "Media Access Control (MAC) address is a unique 48-bit physical identifier on Layer 2."),
        ("Which network topology connects every computer to a single central switch or hub?", "Bus Topology", "Star Topology", "Ring Topology", "Mesh Topology", "B", "Easy", "In Star topology, every peripheral node is connected directly to a central hub or switch."),

        # Medium (10)
        ("What is the primary difference between TCP and UDP?", "TCP is connectionless; UDP establishes handshakes", "TCP guarantees ordered, reliable packet delivery; UDP is connectionless and faster", "TCP only works on LANs", "UDP encrypts all traffic by default", "B", "Medium", "TCP uses sequence numbers and ACKs for reliability; UDP trades reliability for low latency."),
        ("How many bits are in an IPv4 address compared to an IPv6 address?", "IPv4 has 32 bits; IPv6 has 128 bits", "IPv4 has 16 bits; IPv6 has 64 bits", "IPv4 has 64 bits; IPv6 has 256 bits", "IPv4 has 128 bits; IPv6 has 32 bits", "A", "Medium", "IPv4 uses 32-bit addresses (~4.3 billion); IPv6 expands to 128-bit addresses."),
        ("What protocol is used to map an IPv4 address to a local physical MAC address?", "DNS", "ARP", "NAT", "DHCP", "B", "Medium", "Address Resolution Protocol (ARP) translates Layer 3 IP addresses to Layer 2 MAC addresses."),
        ("What does NAT (Network Address Translation) allow a home router to do?", "Boost Wi-Fi signal strength", "Allow multiple private local devices to share a single public IP address", "Encrypt all browser history", "Block all incoming emails", "B", "Medium", "NAT maps multiple private internal IP addresses to a single public IP facing the internet."),
        ("What is the default subnet mask for a standard Class C IPv4 network (/24)?", "255.0.0.0", "255.255.0.0", "255.255.255.0", "255.255.255.255", "C", "Medium", "A /24 prefix corresponds to subnet mask 255.255.255.0, allowing up to 254 usable hosts."),
        ("Which HTTP response status code indicates 'Not Found'?", "200", "301", "404", "500", "C", "Medium", "404 indicates the requested web resource could not be found on the server."),
        ("What is the three-way handshake sequence used to establish a TCP connection?", "SYN, SYN-ACK, ACK", "ACK, SYN, FIN", "HELLO, WAIT, START", "CONNECT, ACCEPT, READY", "A", "Medium", "TCP establishes connections using SYN (Synchronize), SYN-ACK, and ACK (Acknowledge)."),
        ("Which port is standard for secure SSH (Secure Shell) remote terminal access?", "21", "22", "23", "25", "B", "Medium", "Port 22 is the standard port for Secure Shell (SSH) communication."),
        ("At which layer of the OSI model does a network router operate?", "Layer 1 (Physical)", "Layer 2 (Data Link)", "Layer 3 (Network)", "Layer 4 (Transport)", "C", "Medium", "Routers inspect Layer 3 IP packet headers to route traffic between distinct subnets."),
        ("What is the maximum payload size (MTU) of a standard Ethernet frame?", "512 bytes", "1024 bytes", "1500 bytes", "4096 bytes", "C", "Medium", "Standard Ethernet Maximum Transmission Unit (MTU) payload is 1500 bytes.")
    ]

    networking_entrance = [
        # Easy (10)
        ("What does LAN stand for in computer networking?", "Local Area Network", "Logical Access Node", "Large Array Network", "Linked Application Network", "A", "Easy", "A Local Area Network (LAN) connects computers within a limited geographical area like a school."),
        ("What does WAN stand for?", "Wireless Access Network", "Wide Area Network", "Web Application Node", "Worldwide Array Network", "B", "Easy", "A Wide Area Network (WAN) spans broad geographic areas; the Internet is the largest WAN."),
        ("What device connects a local home or school network to the Internet Service Provider (ISP)?", "Modem", "Monitor", "Printer", "Soundcard", "A", "Easy", "A modem modulates and demodulates network signals between the ISP and local router."),
        ("What wireless networking technology standard is commonly known as Wi-Fi?", "IEEE 802.3", "IEEE 802.11", "IEEE 802.15", "Bluetooth 5.0", "B", "Easy", "IEEE 802.11 defines standards for wireless local area networking (Wi-Fi)."),
        ("What security device monitors and filters incoming and outgoing network traffic?", "Repeater", "Firewall", "Hub", "Splitter", "B", "Easy", "A firewall enforces security rules to block unauthorized or malicious traffic."),
        ("What does URL stand for in web browsing?", "Universal Resource Locator", "Uniform Resource Locator", "Unified Routing Link", "User Request Link", "B", "Easy", "Uniform Resource Locator (URL) specifies the web address of a resource on the Internet."),
        ("Which standard connector type is used on Cat5e and Cat6 twisted-pair Ethernet cables?", "USB Type-C", "RJ-45", "HDMI", "VGA", "B", "Easy", "RJ-45 (Registered Jack 45) is the 8-pin connector used for Ethernet cables."),
        ("What protocol is used to transfer files between computers over a network?", "FTP", "HTTP", "DNS", "ICMP", "A", "Easy", "File Transfer Protocol (FTP) is designed for uploading and downloading files."),
        ("What does bandwidth measure in network communication?", "The physical weight of the cables", "The maximum data transfer capacity of a network link over time", "The number of computers on a desk", "The electric voltage", "B", "Easy", "Bandwidth is the maximum rate of data transfer across a network path (e.g. Megabits per second)."),
        ("What network model architecture features client machines requesting services from central servers?", "Peer-to-Peer", "Client-Server", "Ad-hoc", "Broadcast", "B", "Easy", "Client-Server architecture centralizes resources and authentication on designated servers."),

        # Medium (10)
        ("What is the purpose of the 'traceroute' (or 'tracert' in Windows) utility?", "To measure hard drive read speeds", "To show the path and measure transit delays of packets across IP hops to a destination", "To change your IP address", "To crack Wi-Fi passwords", "B", "Medium", "Traceroute traces the route that IP packets take to reach a destination host."),
        ("Which protocol is standard for sending outgoing email from a client to an email server?", "POP3", "IMAP", "SMTP", "HTTP", "C", "Medium", "Simple Mail Transfer Protocol (SMTP, port 25/587) handles outgoing email dispatch."),
        ("What is the difference between POP3 and IMAP for receiving emails?", "POP3 downloads and removes emails from the server; IMAP synchronizes emails across multiple devices", "IMAP is older and slower", "POP3 supports folders while IMAP does not", "They are identical protocols", "A", "Medium", "IMAP keeps messages on the mail server so multiple clients stay synchronized."),
        ("What does a network switch use to make forwarding decisions on a LAN?", "IP routing table", "MAC address table (CAM table)", "DNS cache", "URL whitelist", "B", "Medium", "Layer 2 switches inspect incoming Ethernet frames and build MAC address tables."),
        ("What is latency in computer networking?", "The total amount of data downloaded", "The time delay taken for data to travel from source to destination", "The strength of the Wi-Fi signal", "The frequency of the CPU", "B", "Medium", "Latency measures the round-trip or one-way delay of packet transmission in milliseconds."),
        ("What private IP address range is defined by RFC 1918 for Class A private networks?", "10.0.0.0 to 10.255.255.255", "172.16.0.0 to 172.31.255.255", "192.168.0.0 to 192.168.255.255", "224.0.0.0 to 239.255.255.255", "A", "Medium", "10.0.0.0/8 is reserved for private internal networks."),
        ("What is the purpose of VLAN (Virtual Local Area Network) technology?", "To connect to Bluetooth headsets", "To logically segment and isolate devices on the same physical switch into separate broadcast domains", "To boost Internet download speeds", "To replace Ethernet cables with fiber", "B", "Medium", "VLANs divide a single physical network into multiple isolated virtual networks for security and traffic control."),
        ("Which security protocol is currently considered the most secure standard for home Wi-Fi?", "WEP", "WPA", "WPA2", "WPA3", "D", "Medium", "WPA3 (Wi-Fi Protected Access 3) provides the latest robust encryption and brute-force protection."),
        ("What is packet loss in network performance?", "When physical cables are stolen", "When data packets traversing a network fail to reach their destination", "When a computer runs out of RAM", "When the monitor goes black", "B", "Medium", "Packet loss occurs when transmitted packets are dropped due to network congestion or errors."),
        ("What does TTL (Time to Live) in an IPv4 header do?", "Sets the time zone on the receiver", "Prevents packets from circulating endlessly in routing loops by decrementing at each router hop", "Measures battery percentage", "Encrypts the packet payload", "B", "Medium", "TTL is decremented by 1 at every router hop; when it reaches 0, the packet is discarded.")
    ]

    # Build Networking Questions
    for i, q in enumerate(networking_classroomB, 1):
        task_num = ((i - 1) % 5) + 1
        questions.append({
            "id": f"Q_NET_A_{i:03d}",
            "text": q[0], "optionA": q[1], "optionB": q[2], "optionC": q[3], "optionD": q[4],
            "correctAnswer": q[5], "category": "Networking", "room": "classroomB",
            "difficulty": q[6], "rewardType": "Item", "rewardValue": "Electric Fuse",
            "explanation": q[7], "active": True, "questionType": "MCQ", "taskNumber": task_num
        })

    for i, q in enumerate(networking_entrance, 1):
        task_num = ((i - 1) % 5) + 1
        questions.append({
            "id": f"Q_NET_B_{i:03d}",
            "text": q[0], "optionA": q[1], "optionB": q[2], "optionC": q[3], "optionD": q[4],
            "correctAnswer": q[5], "category": "Networking", "room": "entrance",
            "difficulty": q[6], "rewardType": "Item", "rewardValue": "Flashlight Battery",
            "explanation": q[7], "active": True, "questionType": "MCQ", "taskNumber": task_num
        })

    # =========================================================================
    # CATEGORY 5: GENERAL KNOWLEDGE (40 Questions - 20 Easy, 20 Medium)
    # Rooms: library (20), hall (20)
    # =========================================================================
    general_library = [
        # Easy (10)
        ("Which is the largest ocean on planet Earth by surface area?", "Atlantic Ocean", "Indian Ocean", "Arctic Ocean", "Pacific Ocean", "D", "Easy", "The Pacific Ocean covers over 30% of the Earth's total surface area."),
        ("What is the capital city of France?", "Rome", "Berlin", "Paris", "Madrid", "C", "Easy", "Paris is the capital and largest city of France."),
        ("Who painted the world-famous portrait known as the Mona Lisa?", "Vincent van Gogh", "Leonardo da Vinci", "Pablo Picasso", "Claude Monet", "B", "Easy", "Leonardo da Vinci painted the Mona Lisa during the Italian Renaissance."),
        ("Which continent is the Sahara Desert located on?", "Asia", "Africa", "South America", "Australia", "B", "Easy", "The Sahara Desert is located in northern Africa and is the largest hot desert in the world."),
        ("How many continents are there on Earth?", "5", "6", "7", "8", "C", "Easy", "The seven continents are Asia, Africa, North America, South America, Antarctica, Europe, and Australia."),
        ("What is the tallest mountain above sea level on Earth?", "K2", "Mount Kilimanjaro", "Mount Everest", "Denali", "C", "Easy", "Mount Everest in the Himalayas reaches an elevation of 8,848 meters (29,031 ft)."),
        ("What famous ancient temple complex in Cambodia was built during the reign of King Suryavarman II?", "Borobudur", "Angkor Wat", "Prambanan", "Bagan", "B", "Easy", "Angkor Wat in Siem Reap, Cambodia, was constructed in the early 12th century."),
        ("What is the capital city of Japan?", "Beijing", "Seoul", "Tokyo", "Bangkok", "C", "Easy", "Tokyo is the bustling capital city of Japan."),
        ("What is the primary currency used in Cambodia alongside the US Dollar?", "Baht", "Riel", "Dong", "Kip", "B", "Easy", "The Cambodian Riel (KHR) is the official national currency of Cambodia."),
        ("Who wrote the famous English tragic play 'Romeo and Juliet'?", "Charles Dickens", "William Shakespeare", "Mark Twain", "Jane Austen", "B", "Easy", "William Shakespeare wrote the romantic tragedy 'Romeo and Juliet'."),

        # Medium (10)
        ("Which great river is the longest in the world according to most geographical surveys?", "Amazon River", "Nile River", "Yangtze River", "Mississippi River", "B", "Medium", "The Nile River in northeastern Africa is traditionally recognized as the longest (~6,650 km)."),
        ("What is the largest country in the world by land area?", "Canada", "China", "Russia", "United States", "C", "Medium", "Russia covers over 17 million square kilometers, spanning Eastern Europe and northern Asia."),
        ("Which civilization built Machu Picchu high in the Andes mountains?", "Aztec Civilization", "Inca Civilization", "Maya Civilization", "Olmec Civilization", "B", "Medium", "The Inca Empire built the citadel of Machu Picchu in Peru during the 15th century."),
        ("What is the capital city of Australia?", "Sydney", "Melbourne", "Canberra", "Brisbane", "C", "Medium", "Canberra was selected as the purpose-built federal capital of Australia in 1908."),
        ("Who discovered the antibiotic Penicillin in 1928?", "Louis Pasteur", "Alexander Fleming", "Marie Curie", "Robert Koch", "B", "Medium", "Alexander Fleming discovered penicillin produced by Penicillium notatum mold."),
        ("What historic treaty marked the end of World War I in 1919?", "Treaty of Versailles", "Treaty of Paris", "Treaty of Ghent", "Treaty of Utrecht", "A", "Medium", "The Treaty of Versailles formally concluded the state of war between Germany and the Allied Powers."),
        ("What is the most abundant gas in Earth's atmosphere, making up approximately 78%?", "Oxygen", "Carbon Dioxide", "Argon", "Nitrogen", "D", "Medium", "Nitrogen (N2) comprises roughly 78.08% of Earth's atmosphere by volume."),
        ("Which ancient wonder of the world was located in Alexandria, Egypt, serving as a beacon for sailors?", "Hanging Gardens", "Lighthouse of Alexandria (Pharos)", "Colossus of Rhodes", "Statue of Zeus", "B", "Medium", "The Lighthouse of Alexandria was an architectural marvel built during the Ptolemaic Kingdom."),
        ("What great freshwater lake in Cambodia is connected to the Mekong River and expands dramatically during monsoon season?", "Inle Lake", "Tonle Sap Lake", "Songkhla Lake", "Laguna de Bay", "B", "Medium", "The Tonle Sap Lake is the largest freshwater lake in Southeast Asia and a UNESCO Biosphere Reserve."),
        ("Which famous scientist developed the General Theory of Relativity?", "Isaac Newton", "Albert Einstein", "Niels Bohr", "Galileo Galilei", "B", "Medium", "Albert Einstein published the General Theory of Relativity in 1915.")
    ]

    general_hall = [
        # Easy (10)
        ("What is the closest planet to the Sun in our solar system?", "Venus", "Earth", "Mercury", "Mars", "C", "Easy", "Mercury orbits closest to the Sun at an average distance of ~58 million km."),
        ("What is the capital city of Cambodia?", "Siem Reap", "Battambang", "Phnom Penh", "Sihanoukville", "C", "Easy", "Phnom Penh is the capital and most populous city of Cambodia."),
        ("Which animal is known as the 'King of the Jungle' in popular culture?", "Tiger", "Elephant", "Lion", "Gorilla", "C", "Easy", "Lions have traditionally been dubbed the 'King of the Jungle'."),
        ("How many days are in a leap year?", "364", "365", "366", "367", "C", "Easy", "A leap year has 366 days, with an extra day added to February (Feb 29)."),
        ("What is the color of an emerald gemstone?", "Red", "Blue", "Green", "Yellow", "C", "Easy", "Emeralds are green varieties of the mineral beryl colored by trace amounts of chromium."),
        ("Which country is famous for the ancient Pyramids of Giza?", "Greece", "Egypt", "Italy", "Turkey", "B", "Easy", "The Pyramids of Giza were built in ancient Egypt on the outskirts of modern Cairo."),
        ("What instrument is used to measure atmospheric air pressure?", "Thermometer", "Barometer", "Hygrometer", "Anemometer", "B", "Easy", "A barometer measures atmospheric pressure to assist in weather forecasting."),
        ("What is the national flower of Cambodia?", "Lotus", "Romduol", "Jasmine", "Orchid", "B", "Easy", "The fragrant yellowish-white Romduol (Mitrella mesnyi) was proclaimed Cambodia's national flower in 2005."),
        ("What is the primary language spoken in Brazil?", "Spanish", "Portuguese", "French", "English", "B", "Easy", "Portuguese is the official language of Brazil, reflecting its colonial history."),
        ("What is the freezing point of water on the Fahrenheit scale?", "0 F", "32 F", "100 F", "212 F", "B", "Easy", "Water freezes at 32 degrees Fahrenheit (0 degrees Celsius)."),

        # Medium (10)
        ("What is the largest living mammal currently on Earth?", "African Elephant", "Blue Whale", "Colossal Squid", "Giraffe", "B", "Medium", "The Blue Whale (Balaenoptera musculus) can reach up to 30 meters and weigh over 180 metric tons."),
        ("In what year did the Apollo 11 mission successfully land the first humans on the Moon?", "1965", "1969", "1972", "1975", "B", "Medium", "Neil Armstrong and Buzz Aldrin walked on the Moon on July 20, 1969."),
        ("What is the name of the ancient trade route connecting China with the Mediterranean world?", "Amber Road", "Silk Road", "Spice Route", "Incense Route", "B", "Medium", "The Silk Road was an ancient network of Eurasian trade routes connecting East Asia with Europe."),
        ("Which country gifted the Statue of Liberty to the United States in the late 19th century?", "United Kingdom", "France", "Spain", "Germany", "B", "Medium", "France gifted the Statue of Liberty to commemorate the centennial of the US Declaration of Independence."),
        ("What is the deepest known oceanic trench on Earth?", "Java Trench", "Puerto Rico Trench", "Mariana Trench", "Tonga Trench", "C", "Medium", "The Mariana Trench in the western Pacific reaches a maximum depth of ~11,000 meters at the Challenger Deep."),
        ("What international organization was founded in 1945 following World War II to maintain peace and security?", "League of Nations", "United Nations", "NATO", "World Bank", "B", "Medium", "The United Nations (UN) was established in 1945 by 51 founding countries."),
        ("Which classical composer continued to compose masterpieces like the Ninth Symphony after becoming completely deaf?", "Wolfgang Amadeus Mozart", "Ludwig van Beethoven", "Johann Sebastian Bach", "Frederic Chopin", "B", "Medium", "Beethoven composed his revolutionary late works, including the Ninth Symphony, while deaf."),
        ("What is the study of celestial bodies such as stars, planets, and galaxies called?", "Astrology", "Astronomy", "Geology", "Meteorology", "B", "Medium", "Astronomy is the scientific study of the universe, celestial objects, and space phenomena."),
        ("Which country has the highest population in the world as of recent United Nations estimates?", "China", "India", "United States", "Indonesia", "B", "Medium", "India surpassed China as the world's most populous nation in 2023."),
        ("What is the primary constitutional role of the King of Cambodia under the 1993 Constitution?", "Head of Government with executive powers", "Reigns as Head of State but does not govern", "Chief Justice of Supreme Court", "Commander of Police only", "B", "Medium", "Under the 1993 Constitution, the King of Cambodia reigns but does not govern as Head of State.")
    ]

    # Build General Knowledge Questions
    for i, q in enumerate(general_library, 1):
        task_num = ((i - 1) % 5) + 1
        questions.append({
            "id": f"Q_GEN_A_{i:03d}",
            "text": q[0], "optionA": q[1], "optionB": q[2], "optionC": q[3], "optionD": q[4],
            "correctAnswer": q[5], "category": "General Knowledge", "room": "library",
            "difficulty": q[6], "rewardType": "Key" if i == 5 else "Item",
            "rewardValue": "Library Key" if i == 5 else "Holy Charm",
            "explanation": q[7], "active": True, "questionType": "MCQ", "taskNumber": task_num
        })

    for i, q in enumerate(general_hall, 1):
        task_num = ((i - 1) % 5) + 1
        questions.append({
            "id": f"Q_GEN_B_{i:03d}",
            "text": q[0], "optionA": q[1], "optionB": q[2], "optionC": q[3], "optionD": q[4],
            "correctAnswer": q[5], "category": "General Knowledge", "room": "hall",
            "difficulty": q[6], "rewardType": "Item", "rewardValue": "Flashlight Battery",
            "explanation": q[7], "active": True, "questionType": "MCQ", "taskNumber": task_num
        })

    return questions

if __name__ == "__main__":
    qs = create_all_questions()
    print(f"Total questions generated: {len(qs)}")
    
    # Validation checks
    cat_counts = {}
    diff_counts = {}
    room_counts = {}
    for q in qs:
        c = q["category"]
        d = q["difficulty"]
        r = q["room"]
        cat_counts[c] = cat_counts.get(c, 0) + 1
        diff_counts[d] = diff_counts.get(d, 0) + 1
        room_counts[r] = room_counts.get(r, 0) + 1
    
    print("\n--- CATEGORY BREAKDOWN ---")
    for c, cnt in sorted(cat_counts.items()):
        print(f"  {c}: {cnt}")
        
    print("\n--- DIFFICULTY BREAKDOWN ---")
    for d, cnt in sorted(diff_counts.items()):
        print(f"  {d}: {cnt}")
        
    print("\n--- ROOM BREAKDOWN ---")
    for r, cnt in sorted(room_counts.items()):
        print(f"  {r}: {cnt}")

    # Write to target files
    paths = [
        "src/main/resources/questions/questions.json",
        "questions/questions.json"
    ]
    for p in paths:
        os.makedirs(os.path.dirname(p), exist_ok=True)
        with open(p, "w", encoding="utf-8") as f:
            json.dump(qs, f, indent=2, ensure_ascii=False)
        print(f"Wrote {len(qs)} questions to {p}")
