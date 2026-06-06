export class CourseConvertor {
    private year: string; 
    private course: number;

    constructor(date: string, course: number) {
        this.year = date;
        this.course = course;
    }

    dateOfReceipt() {
        const date = new Date(Number(this.year) - this.course, 8, 1);
    }

    convertByCourse() {

    }
}