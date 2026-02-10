export interface PaymentRequest {
  reservation: any;
  payment: {
    method: string;
    amount: number;
  };
}
