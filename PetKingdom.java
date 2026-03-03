import java.util.*;

public class PetKingdom {

    // ====== ENUMS ======
    enum PetStatus { IN_STOCK, SOLD }
    enum MemberLevel { NORMAL, VIP }
    enum ActionType {
        ADD_PET, REMOVE_PET,
        ADD_CUSTOMER,
        ENQUEUE_SPA, DEQUEUE_SPA
    }

    // ====== ENTITIES ======
    static class Pet {
        private String id;
        private String name;
        private String species;
        private String breed;
        private int age;
        private double price;
        private PetStatus status;

        public Pet(String id, String name, String species, String breed, int age, double price) {
            this.id = id;
            this.name = name;
            this.species = species;
            this.breed = breed;
            this.age = age;
            this.price = price;
            this.status = PetStatus.IN_STOCK;
        }

        public String getId() { return id; }
        public PetStatus getStatus() { return status; }
        public void setStatus(PetStatus status) { this.status = status; }

        @Override
        public String toString() {
            return "Pet{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", species='" + species + '\'' +
                    ", breed='" + breed + '\'' +
                    ", age=" + age +
                    ", price=" + price +
                    ", status=" + status +
                    '}';
        }
    }

    static class Customer {
        private String customerId;
        private String fullName;
        private String phone;
        private MemberLevel level;
        private double discountRate;

        public Customer(String customerId, String fullName, String phone, MemberLevel level) {
            this.customerId = customerId;
            this.fullName = fullName;
            this.phone = phone;
            this.level = level;
            this.discountRate = (level == MemberLevel.VIP) ? 0.1 : 0.0;
        }

        public String getCustomerId() { return customerId; }
        public String getPhone() { return phone; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Customer customer = (Customer) o;
            // unique theo customerId hoặc phone
            return Objects.equals(customerId, customer.customerId)
                    || Objects.equals(phone, customer.phone);
        }

        @Override
        public int hashCode() {
            return Objects.hash(customerId, phone);
        }

        @Override
        public String toString() {
            return "Customer{" +
                    "customerId='" + customerId + '\'' +
                    ", fullName='" + fullName + '\'' +
                    ", phone='" + phone + '\'' +
                    ", level=" + level +
                    ", discountRate=" + (discountRate * 100) + "%" +
                    '}';
        }
    }

    // ====== GENERIC MANAGERS ======
    interface Identifiable {
        String getId();
    }

    static class InventoryManager<T extends Identifiable> {
        private final List<T> items = new ArrayList<>();

        public void add(T item) { items.add(item); }

        public List<T> getAll() { return Collections.unmodifiableList(items); }

        public T findById(String id) {
            for (T t : items) {
                if (t.getId().equalsIgnoreCase(id)) return t;
            }
            return null;
        }

        public boolean removeById(String id) {
            Iterator<T> it = items.iterator();
            while (it.hasNext()) {
                T t = it.next();
                if (t.getId().equalsIgnoreCase(id)) {
                    it.remove();
                    return true;
                }
            }
            return false;
        }

        public void addBack(T item) { items.add(item); }
    }

    static class SpaQueueManager<T> {
        private final Queue<T> queue = new ArrayDeque<>();

        public void enqueue(T item) { queue.offer(item); }
        public T dequeue() { return queue.poll(); }
        public T peek() { return queue.peek(); }
        public boolean isEmpty() { return queue.isEmpty(); }
        public int size() { return queue.size(); }

        public void enqueueFrontForUndo(T item) {
            if (queue instanceof ArrayDeque) {
                ArrayDeque<T> dq = (ArrayDeque<T>) queue;
                dq.addFirst(item);
            } else {
                ArrayDeque<T> dq = new ArrayDeque<>();
                dq.addFirst(item);
                dq.addAll(queue);
                queue.clear();
                queue.addAll(dq);
            }
        }

        public List<T> snapshot() { return new ArrayList<>(queue); }
    }

    static class ActionStackManager<T> {
        private final Deque<T> stack = new ArrayDeque<>();
        public void push(T action) { stack.push(action); }
        public T pop() { return stack.poll(); }
        public T peek() { return stack.peek(); }
        public boolean isEmpty() { return stack.isEmpty(); }
        public List<T> snapshot() { return new ArrayList<>(stack); }
    }

    static class PetEntity extends Pet implements Identifiable {
        public PetEntity(String id, String name, String species, String breed, int age, double price) {
            super(id, name, species, breed, age, price);
        }
        @Override
        public String getId() { return super.getId(); }
    }

    // ====== CUSTOMER REGISTRY (Set + Map) ======
    static class CustomerRegistry {
        private final Set<Customer> uniqueCustomers = new HashSet<>();
        private final Map<String, Customer> byId = new HashMap<>();

        public boolean register(Customer c) {
            if (byId.containsKey(c.getCustomerId())) return false;
            for (Customer existing : uniqueCustomers) {
                if (existing.getPhone().equals(c.getPhone())) return false;
            }
            boolean added = uniqueCustomers.add(c);
            if (added) byId.put(c.getCustomerId(), c);
            return added;
        }

        public Customer findById(String customerId) {
            return byId.get(customerId);
        }

        public List<Customer> getAll() {
            return new ArrayList<>(uniqueCustomers);
        }

        public void addBack(Customer c) {
            uniqueCustomers.add(c);
            byId.put(c.getCustomerId(), c);
        }

        public boolean removeById(String customerId) {
            Customer c = byId.remove(customerId);
            if (c == null) return false;
            uniqueCustomers.remove(c);
            return true;
        }
    }

    // ====== ACTION + UNDO ======
    interface UndoableAction {
        ActionType getType();
        String getDescription();
        void undo();
    }

    static class SimpleUndoAction implements UndoableAction {
        private final ActionType type;
        private final String description;
        private final Runnable undoJob;

        public SimpleUndoAction(ActionType type, String description, Runnable undoJob) {
            this.type = type;
            this.description = description;
            this.undoJob = undoJob;
        }

        @Override public ActionType getType() { return type; }
        @Override public String getDescription() { return description; }
        @Override public void undo() { undoJob.run(); }

        @Override
        public String toString() {
            return "[" + type + "] " + description;
        }
    }

    // ====== APP ======
    private final Scanner sc = new Scanner(System.in);

    private final InventoryManager<PetEntity> inventory = new InventoryManager<>();
    private final CustomerRegistry customers = new CustomerRegistry();
    private final SpaQueueManager<PetEntity> spaQueue = new SpaQueueManager<>();
    private final ActionStackManager<UndoableAction> actionLog = new ActionStackManager<>();

    public static void main(String[] args) {
        new PetKingdom().run();
    }

    private void run() {
        seedData();
        while (true) {
            printMenu();
            int choice = readInt("Chọn: ");
            switch (choice) {
                case 1 -> addPet();
                case 2 -> showPets();
                case 3 -> findPet();
                case 4 -> removePet();
                case 5 -> registerCustomer();
                case 6 -> findCustomer();
                case 7 -> enqueueSpa();
                case 8 -> processSpa();
                case 9 -> showActions();
                case 10 -> undoLast();
                case 0 -> {
                    System.out.println("Thoát chương trình.");
                    return;
                }
                default -> System.out.println("Lựa chọn không hợp lệ.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== PET KINGDOM ===");
        System.out.println("1. Thêm mới thú cưng (List)");
        System.out.println("2. Hiển thị danh sách thú cưng");
        System.out.println("3. Tìm thú cưng theo ID");
        System.out.println("4. Xóa thú cưng (bán) theo ID");
        System.out.println("5. Đăng ký khách hàng thân thiết (Set/Map)");
        System.out.println("6. Tra cứu khách hàng theo mã");
        System.out.println("7. Tiếp nhận thú cưng vào Spa (Queue)");
        System.out.println("8. Xử lý Spa (FIFO)");
        System.out.println("9. Xem nhật ký hoạt động (Stack)");
        System.out.println("10. Undo thao tác gần nhất (Stack)");
        System.out.println("0. Thoát");
    }

    private void seedData() {
        inventory.add(new PetEntity("P001", "Milu", "Dog", "Poodle", 2, 300.0));
        inventory.add(new PetEntity("P002", "Mimi", "Cat", "British Shorthair", 1, 250.0));
        customers.register(new Customer("C001", "Nguyen Van A", "0900000001", MemberLevel.NORMAL));
        customers.register(new Customer("C002", "Tran Thi B", "0900000002", MemberLevel.VIP));
    }

    // ====== FEATURES ======
    private void addPet() {
        System.out.println("\n-- Thêm thú cưng --");
        String id = readNonEmpty("ID: ");
        if (inventory.findById(id) != null) {
            System.out.println("ID đã tồn tại!");
            return;
        }
        String name = readNonEmpty("Tên: ");
        String species = readNonEmpty("Loài: ");
        String breed = readNonEmpty("Giống: ");
        int age = readInt("Tuổi: ");
        double price = readDouble("Giá: ");

        PetEntity pet = new PetEntity(id, name, species, breed, age, price);
        inventory.add(pet);

        actionLog.push(new SimpleUndoAction(
                ActionType.ADD_PET,
                "Đã thêm thú cưng " + id,
                () -> inventory.removeById(id)
        ));

        System.out.println("Thêm thành công!");
    }

    private void showPets() {
        System.out.println("\n-- Danh sách thú cưng --");
        List<PetEntity> pets = inventory.getAll();
        if (pets.isEmpty()) {
            System.out.println("(Trống)");
            return;
        }
        for (PetEntity p : pets) System.out.println(p);
    }

    private void findPet() {
        System.out.println("\n-- Tìm thú cưng --");
        String id = readNonEmpty("Nhập ID: ");
        PetEntity pet = inventory.findById(id);
        System.out.println(pet == null ? "Không tìm thấy." : pet);
    }

    private void removePet() {
        System.out.println("\n-- Xóa/Bán thú cưng --");
        String id = readNonEmpty("Nhập ID: ");
        PetEntity pet = inventory.findById(id);
        if (pet == null) {
            System.out.println("Không tìm thấy.");
            return;
        }
        boolean removed = inventory.removeById(id);
        if (!removed) {
            System.out.println("Xóa thất bại.");
            return;
        }

        actionLog.push(new SimpleUndoAction(
                ActionType.REMOVE_PET,
                "Đã xóa (bán) thú cưng " + id,
                () -> inventory.addBack(pet)
        ));

        System.out.println("Đã xóa khỏi kho.");
    }

    private void registerCustomer() {
        System.out.println("\n-- Đăng ký khách hàng --");
        String cid = readNonEmpty("Mã KH: ");
        String name = readNonEmpty("Họ tên: ");
        String phone = readNonEmpty("SĐT: ");
        int levelChoice = readInt("Level (1=Normal, 2=VIP): ");
        MemberLevel level = (levelChoice == 2) ? MemberLevel.VIP : MemberLevel.NORMAL;

        Customer c = new Customer(cid, name, phone, level);
        boolean ok = customers.register(c);
        if (!ok) {
            System.out.println("Đăng ký thất bại (trùng mã hoặc SĐT).");
            return;
        }

        actionLog.push(new SimpleUndoAction(
                ActionType.ADD_CUSTOMER,
                "Đã đăng ký khách hàng " + cid,
                () -> customers.removeById(cid)
        ));

        System.out.println("Đăng ký thành công!");
    }

    private void findCustomer() {
        System.out.println("\n-- Tra cứu khách hàng --");
        String cid = readNonEmpty("Nhập mã KH: ");
        Customer c = customers.findById(cid);
        System.out.println(c == null ? "Không tìm thấy." : c);
    }

    private void enqueueSpa() {
        System.out.println("\n-- Tiếp nhận Spa --");
        String petId = readNonEmpty("Nhập ID thú cưng: ");
        PetEntity pet = inventory.findById(petId);
        if (pet == null) {
            System.out.println("Không tìm thấy thú cưng trong kho.");
            return;
        }
        if (pet.getStatus() == PetStatus.SOLD) {
            System.out.println("Thú cưng đã bán, không thể vào spa.");
            return;
        }

        spaQueue.enqueue(pet);

        actionLog.push(new SimpleUndoAction(
                ActionType.ENQUEUE_SPA,
                "Đã đưa thú cưng " + petId + " vào hàng chờ spa",
                () -> {
                    List<PetEntity> snapshot = spaQueue.snapshot();
                    ArrayDeque<PetEntity> rebuilt = new ArrayDeque<>();
                    boolean removedOnce = false;
                    for (PetEntity p : snapshot) {
                        if (!removedOnce && p.getId().equalsIgnoreCase(petId)) {
                            removedOnce = true;
                            continue;
                        }
                        rebuilt.offer(p);
                    }
                    while (!spaQueue.isEmpty()) spaQueue.dequeue();
                    for (PetEntity p : rebuilt) spaQueue.enqueue(p);
                }
        ));

        System.out.println("Đã tiếp nhận! Hiện có " + spaQueue.size() + " thú cưng chờ spa.");
    }

    private void processSpa() {
        System.out.println("\n-- Xử lý Spa (FIFO) --");
        PetEntity current = spaQueue.dequeue();
        if (current == null) {
            System.out.println("Không có thú cưng nào đang chờ.");
            return;
        }

        actionLog.push(new SimpleUndoAction(
                ActionType.DEQUEUE_SPA,
                "Đang phục vụ thú cưng " + current.getId() + " tại spa",
                () -> spaQueue.enqueueFrontForUndo(current)
        ));

        System.out.println("Đang tắm rửa/cắt tỉa: " + current);
        System.out.println("Còn lại " + spaQueue.size() + " thú cưng trong hàng chờ.");
    }

    private void showActions() {
        System.out.println("\n-- Nhật ký hoạt động (gần nhất trước) --");
        List<UndoableAction> actions = actionLog.snapshot();
        if (actions.isEmpty()) {
            System.out.println("(Chưa có hành động)");
            return;
        }
        for (UndoableAction a : actions) System.out.println(a);
    }

    private void undoLast() {
        System.out.println("\n-- Undo --");
        UndoableAction last = actionLog.pop();
        if (last == null) {
            System.out.println("Không có hành động để undo.");
            return;
        }
        last.undo();
        System.out.println("Đã hoàn tác: " + last.getDescription());
    }

    // ====== INPUT HELPERS ======
    private String readNonEmpty(String label) {
        while (true) {
            System.out.print(label);
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("Không được để trống!");
        }
    }

    private int readInt(String label) {
        while (true) {
            System.out.print(label);
            String s = sc.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Vui lòng nhập số nguyên hợp lệ!");
            }
        }
    }

    private double readDouble(String label) {
        while (true) {
            System.out.print(label);
            String s = sc.nextLine().trim();
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                System.out.println("Vui lòng nhập số hợp lệ!");
            }
        }
    }
}