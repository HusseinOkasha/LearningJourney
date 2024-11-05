import TaskStatus from "../enums/TaskStatus";

export default interface Task {
  title: string;
  description: string;
  taskStatus: TaskStatus;
}
